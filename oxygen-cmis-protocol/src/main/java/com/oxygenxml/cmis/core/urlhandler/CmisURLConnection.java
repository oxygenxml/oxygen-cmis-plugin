package com.oxygenxml.cmis.core.urlhandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.CmisURL;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CmisURLConnection extends URLConnection {

  /**
   * CMISAcces Instance.
   */
  private CMISAccess cmisAccess;
  
  /**
   * ResourceController Instance for CMISObjects manipulations.
   */
  private ResourceController resourceController;
  
  /**
   * UserCredentials Instance.
   */
  private UserCredentials credentials;

  /**
   * CmisURLConnection constructor.
   * @param url
   * @param cmisAccess
   * @param credentials
   */
  public CmisURLConnection(URL url, CMISAccess cmisAccess, UserCredentials credentials) {
    super(url);
    this.cmisAccess = cmisAccess;
    this.credentials = credentials;
  }

  /**
   * Generates a CMIS URL for an object.
   * 
   * @param object The object
   * @param ctrl The resource controller.
   * @return The path to the parent folder.
   * 
   */
  public static String generateURLObject(CmisObject object, ResourceController ctrl, String parentPath) {
    // Get and encode server URL
    String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);
    String repository = ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID);

    // Generate first part of custom URL
    CmisURL repoCmisURL; 
    try {
      repoCmisURL = CmisURL.ofRepo(new URL(originalProtocol), repository);
    } catch (MalformedURLException e) {
      // Canot happen.
      throw new RuntimeException(e);
    }
    
    CmisURL objCmisUrl = repoCmisURL.setPath(parentPath + object.getName());

    // Get path of Cmis Object
    List<String> objectPaths = ((FileableCmisObject) object).getPaths();
    for (String objectPath: objectPaths) {
      // Check if path(i) start with path of parent folder
      if (objectPath.startsWith(parentPath)) {
        objCmisUrl = repoCmisURL.setPath(objectPath);
        break;
      }
    }

    return objCmisUrl.toExternalForm();
  }

  /**
   * Overload the one with parent path.
   * 
   * @param object
   * @param ctrl
   * @return the generated Url
   */
  public static String generateURLObject(CmisObject object, ResourceController ctrl) {
    if (log.isDebugEnabled()) {
      log.debug("Generate URL for: " + object.getName());
    }

    // Get and encode server URL
    String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);
    String repository = ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID);

    // Generate first part of custom URL
    CmisURL repoCmisURL; 
    try {
      repoCmisURL = CmisURL.ofRepo(new URL(originalProtocol), repository);
    } catch (MalformedURLException e) {
      // Canot happen.
      throw new RuntimeException(e);
    }

    // Get and apend to URL path of Cmis Object
    List<String> objectPaths = ((FileableCmisObject) object).getPaths();
    if (log.isDebugEnabled()) {
      log.debug("Paths: " + objectPaths);
    }
    CmisURL objCmisUrl;
    if (!objectPaths.isEmpty()) {
      objCmisUrl = repoCmisURL.setPath(objectPaths.get(0));
    } else {
      objCmisUrl = repoCmisURL;
    }
    return objCmisUrl.toExternalForm();
  }

  /**
   * Gets the CmisObject identified by the given URL.
   * 
   * @param url
   *          URL identifying a CMIS resource.
   * 
   * @return The CMIS object identified by the custom URL.
   * 
   * @throws MalformedURLException
   *           If the URL doesn't contain the expected syntax.
   * @throws CmisObjectNotFoundException If the URL doesn't point to an existing object.
   */
  public CmisObject getCMISObject(String url) throws MalformedURLException, CmisObjectNotFoundException {
    // Get from custom URL server URL for connection
    CmisURL cmisUrl = CmisURL.parse(url);

    // Get repository ID from custom URL for connection
    String repoID = cmisUrl.getRepositoryId();

    // Accessing the server using params which we gets
    cmisAccess.connectToRepo(cmisUrl.getServerHttpUrl(), repoID, credentials);
    resourceController = cmisAccess.createResourceController();

    // Get the object path
    String path = cmisUrl.getPath();

    return resourceController.getSession().getObjectByPath(path);
  }

  @Override
  public void connect() throws IOException {
    // Pass
  }

  @Override
  public InputStream getInputStream() throws IOException {
    try {
    Document initialDocument = (Document) getCMISObject(getURL().toExternalForm());
    Document document = initialDocument;
    
    if (document.isVersionable()) {
      document = document.getObjectOfLatestVersion(false);
      
      if (document.isVersionSeriesCheckedOut()) {
        document = getPwcDocument(initialDocument);
      }
    }
    
    ContentStream contentStream = null;
    if (document != null) {
      contentStream = document.getContentStream();
    }
    if (contentStream == null) {
      contentStream = initialDocument.getContentStream();
    }
    
    if (contentStream != null) {
      return contentStream.getStream();
    } else {
      throw new IOException("This document does not have any content");
    }
    } catch (CmisObjectNotFoundException ex) {
      throw new FileNotFoundException(getURL().getPath());
    }
  }

  public Document getPwcDocument(Document document) {
    Document pwcDoc = document;
    if(!Boolean.TRUE.equals(document.isLatestVersion())) {
      pwcDoc = document.getObjectOfLatestVersion(false);
    }
   
    // If the CMS provides an ID for the PWC object we fetch it (Alfresco implementation)
    // otherwise we consider the latest version to be the PWC (SharePoint implementation)
    if (document.getVersionSeriesCheckedOutId() != null) {
        String pwcId = document.getVersionSeriesCheckedOutId();
        pwcDoc = (Document) resourceController.getSession().getObject(pwcId);
    }
    return pwcDoc;
  }

  
  @Override
  public OutputStream getOutputStream() throws IOException {
    return new ByteArrayOutputStream() {
      @Override
      public void close() throws IOException {
        Document document = null;
        String documentUrl = null;
        boolean newDocument = false;

        try {
          documentUrl = getURL().toExternalForm();
          document = (Document) getCMISObject(documentUrl);

        } catch (CmisObjectNotFoundException | CmisConnectionException e) {
          // If created document doesn't exist we create one
          documentUrl = createDocument();
          document = (Document) getCMISObject(documentUrl);
          newDocument = true;
        }

        // All bytes have been written.
        byte[] byteArray = toByteArray();

        // If this document is a null document it won't have a mime type.
        String contentStreamMimeType = document.getContentStreamMimeType();
        if (contentStreamMimeType == null) {
          contentStreamMimeType = "text/xml";
        }
        
        ContentStreamImpl contentStream = new ContentStreamImpl(document.getName(),
            BigInteger.valueOf(byteArray.length), contentStreamMimeType,
            new ByteArrayInputStream(byteArray));

        /**
         * Here we check if document is versionable and do operation. For
         * versionable document we get the PWC to save the new content stream.
         * After this we check in (as minor) document if it wasn't check out. If
         * document is newly we check-in it as major version and saving the
         * template content stream from client-side.
         * 
         */
        trySave(document, newDocument, contentStream);
      }

      private void trySave(Document document, boolean newDocument, ContentStreamImpl contentStream) throws IOException {
        try {
          if (!document.isVersionable()) {
            document.setContentStream(contentStream, true);
          } else {
            // we get a working document either by getting the already checked out one
            // or by checking it out
            Document pwcDoc = null;
            boolean alreadyCheckedOut = true;
            if (document.isVersionSeriesCheckedOut()) {
              pwcDoc = getPwcDocument(document);
            } else {
              ObjectId pwcID = document.checkOut();
              pwcDoc = (Document) resourceController.getSession().getObject(pwcID);
              alreadyCheckedOut = false;
            }

            pwcDoc.setContentStream(contentStream, true);

            if (newDocument) {
              pwcDoc.checkIn(true, null, null, " ");
              deleteUselessVersion(document);
            } else if (!alreadyCheckedOut) {
              pwcDoc.checkIn(false, null, null, " ");
            }
          }
        } catch (Exception e) {
          throw new IOException(e.getMessage(), e);
        }
      }
    };
  }

  /**
   * Removing first useless version of newly created document.
   * 
   * @param document
   */
  private void deleteUselessVersion(Document document) {
	 document = document.getObjectOfLatestVersion(false);
	 List<Document> allVersions = document.getAllVersions();
	 Document usellesVersion = allVersions.get(allVersions.size() - 1);
	 resourceController.deleteOneVersionDocument(usellesVersion);
  }
  
  /**
   * Create new document as versionable and generate URL if doesn't exist.
   * 
   * @return the generated URL
   * @throws MalformedURLException
   */
  public String createDocument() throws MalformedURLException {
    CmisURL cmisUrl = CmisURL.parse(url.toExternalForm());

    String folderPath = cmisUrl.getFolderPath();
    String fileName = cmisUrl.getFileName();

    String mimeType = MimeTypes.getMIMEType(cmisUrl.getExtension());
    if (mimeType == "application/octet-stream") {
      mimeType = "text/xml";
    }

    Folder rootFolder = (Folder) cmisAccess.getSession().getObjectByPath(folderPath);
    Document document = resourceController.createEmptyVersionedDocument(
        rootFolder, fileName, mimeType, VersioningState.MINOR);

    return generateURLObject(document, resourceController, folderPath);
  }

  public boolean canCheckoutDocument(Document doc) {
    Boolean canSetContentStream = doc.hasAllowableAction(Action.CAN_SET_CONTENT_STREAM);
    boolean isSharePoint = getCMISAccess().isSharePoint();

    String versionSeriesCheckedOutBy = doc.getVersionSeriesCheckedOutBy();

    // With SharePoint the value of the versionSeriesCheckedOutBy attribute is the display name
    // of the user and not the login name. Therefore when connected to SharePoint we check
    // if the logged in user can set content stream for the document
    // For other CMSes we verify that the user who checked out the document is the logged in user 
    return (canSetContentStream && isSharePoint) || versionSeriesCheckedOutBy == null 
        || getUserCredentials().getUsername().equals(versionSeriesCheckedOutBy);
  }
  
  /**
   * 
   * @param connectionUrl
   * @return ResourceController
   * @throws MalformedURLException
   */
  public ResourceController getResourceController(String connectionUrl) throws MalformedURLException {
    getCMISObject(connectionUrl);
    return resourceController;
  }

  /**
   * 
   * @return CMISAccess
   */
  public CMISAccess getCMISAccess() {
    return cmisAccess;
  }

  /**
   * 
   * @return UserCredentials
   */
  public UserCredentials getUserCredentials() {
    return credentials;
  }
}
