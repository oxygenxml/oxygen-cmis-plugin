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
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.CmisURL;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.basic.util.URLUtil;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;

public class CmisURLConnection extends URLConnection {
  private static final String SLASH_SYMBOL = "/";

  /**
   * Logging support.
   */
  private static Logger logger = Logger.getLogger(CmisURLConnection.class);

  private CMISAccess cmisAccess;
  private ResourceController resourceController;
  private UserCredentials credentials;

  // CONSTRUCTOR
  public CmisURLConnection(URL url, CMISAccess cmisAccess, UserCredentials credentials) {
    super(url);
    this.cmisAccess = cmisAccess;
    this.credentials = credentials;
  }

  /**
   * 
   * @param object
   * @param _ctrl
   * @return
   * @throws UnsupportedEncodingException
   */
  public static String generateURLObject(CmisObject object, ResourceController ctrl, String parentPath) {
    StringBuilder urlb = new StringBuilder();

    // Get and encode server URL
    String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);
    String repository = ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID);
    originalProtocol = URLUtil.encodeURIComponent(originalProtocol);

    // Generate first part of custom URL
    urlb.append((CmisURL.CMIS_PROTOCOL + "://")).append(originalProtocol).append(SLASH_SYMBOL).append(repository);

    Boolean invalidPath = true;
    // Get path of Cmis Object
    List<String> objectPath = ((FileableCmisObject) object).getPaths();

    parentPath = URLUtil.decodeURIComponent(parentPath);
    if (parentPath.contains(repository)) {
      parentPath = parentPath.replace(repository + SLASH_SYMBOL, "");
    }

    // Append object path to URL
    for (int i = 0; i < objectPath.size(); i++) {
      // Check if path(i) start with path of parent folder
      if (objectPath.get(i).startsWith(parentPath)) {
        invalidPath = false;
        for (String pth : objectPath.get(i).split(SLASH_SYMBOL)) {
          if (!pth.isEmpty()) {
            urlb.append(SLASH_SYMBOL).append(URLUtil.encodeURIComponent(pth));
          }
        }
        break;
      }
    }

    if (invalidPath) {
      urlb.append(SLASH_SYMBOL).append(parentPath).append(object.getName());
    }

    return urlb.toString();
  }

  /**
   * Overload the one with parent path.
   * 
   * @param object
   * @param ctrl
   * @return
   */
  public static String generateURLObject(CmisObject object, ResourceController ctrl) {
    StringBuilder urlb = new StringBuilder();

    logger.info("Generate URL for: " + object.getName());

    // Get and encode server URL
    String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);
    String repository = ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID);
    originalProtocol = URLUtil.encodeURIComponent(originalProtocol);

    // Generate first part of custom URL
    urlb.append((CmisURL.CMIS_PROTOCOL + "://")).append(originalProtocol).append(SLASH_SYMBOL).append(repository);

    // Get and apend to URL path of Cmis Object
    List<String> objectPath = ((FileableCmisObject) object).getPaths();
    logger.info("Paths " + objectPath);
    for (int i = 0; i < objectPath.size(); i++) {
      for (String pth : objectPath.get(i).split(SLASH_SYMBOL)) {
        if (!pth.isEmpty()) {
          urlb.append(SLASH_SYMBOL).append(URLUtil.encodeURIComponent(pth));
        }
      }
    }
    return urlb.toString();
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
   */
  public CmisObject getCMISObject(String url)
      throws CmisUnauthorizedException, CmisObjectNotFoundException, MalformedURLException {
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
    // Not implemented
  }

  @Override
  public InputStream getInputStream() throws IOException {
    Document document = (Document) getCMISObject(getURL().toExternalForm());
    Document pwcDoc = null;

    if (document.isVersionSeriesCheckedOut()) {
      String pwcId = document.getVersionSeriesCheckedOutId();
      pwcDoc = (Document) resourceController.getSession().getObject(pwcId);
      
      return pwcDoc.getContentStream().getStream();
    }
    
    if(document.isVersionable()) {
    	document = document.getObjectOfLatestVersion(false);
    }
    
    return document.getContentStream().getStream();
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

        } catch (CmisObjectNotFoundException e) {
          // If created document doesn't exist we create one
          documentUrl = createDocument();
          document = (Document) getCMISObject(documentUrl);
          newDocument = true;
        }

        // All bytes have been written.
        byte[] byteArray = toByteArray();

        ContentStreamImpl contentStream = new ContentStreamImpl(document.getName(),
            BigInteger.valueOf(byteArray.length), document.getContentStreamMimeType(),
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
  public String createDocument() throws MalformedURLException, UnsupportedEncodingException {
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
