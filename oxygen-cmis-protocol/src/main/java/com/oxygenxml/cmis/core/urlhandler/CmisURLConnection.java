package com.oxygenxml.cmis.core.urlhandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.CmisURL;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;

public class CmisURLConnection extends URLConnection {
  /**
   * Logging support.
   */
  private static Logger logger = Logger.getLogger(CmisURLConnection.class);

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
   * @throws UnsupportedEncodingException
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
   * @return
   */
  public static String generateURLObject(CmisObject object, ResourceController ctrl) {
    if (logger.isDebugEnabled()) {
      logger.debug("Generate URL for: " + object.getName());
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
    if (logger.isDebugEnabled()) {
      logger.debug("Paths: " + objectPaths);
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
   * @throws UnsupportedEncodingException
   * @throws UserActionRequiredException
   * @throws CmisObjectNotFoundException If the URL doesn't point to an existing object.
   */
  public CmisObject getCMISObject(String url) throws MalformedURLException, CmisObjectNotFoundException {
    // Get from custom URL server URL for connection
    CmisURL cmisUrl = CmisURL.parse(url);

    // Get repository ID from custom URL for connection
    String repoID = cmisUrl.getRepository();

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
    
    Boolean isVersionSeriesCheckedOut = document.isVersionSeriesCheckedOut();
    if (isVersionSeriesCheckedOut != null && isVersionSeriesCheckedOut) {
      String pwcId = document.getVersionSeriesCheckedOutId();
      document = (Document) resourceController.getSession().getObject(pwcId);
    } else if (document.isVersionable()) {
      document = document.getObjectOfLatestVersion(false);
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
      throw new IOException(ex);
    }
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
        if (!document.isVersionable()) {
          document.setContentStream(contentStream, true);
        } else {
          Document pwcDoc = null;
          boolean wasChecked = false;
          document = document.getObjectOfLatestVersion(false);

          if (document.isVersionSeriesCheckedOut()) {
            String pwcId = document.getVersionSeriesCheckedOutId();
            pwcDoc = (Document) resourceController.getSession().getObject(pwcId);
          } else {
            pwcDoc = (Document) resourceController.getSession().getObject(document.checkOut());
            wasChecked = true;
          }

          pwcDoc.setContentStream(contentStream, true);

          if (newDocument) {
            pwcDoc.checkIn(true, null, null, " ");
            deleteUselessVersion(document);
          } else if (wasChecked) {
            pwcDoc.checkIn(false, null, null, " ");
          }
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
   * @param byteArray
   * @param typeOfDocument
   * 
   * @param document
   * @return
   * @throws UnsupportedEncodingException
   * @throws MalformedURLException
   * @throws IOException
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

  /**
   * 
   * @param connectionUrl
   * @return ResourceController
   * @throws MalformedURLException
   * @throws UnsupportedEncodingException
   * @throws UserActionRequiredException
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
