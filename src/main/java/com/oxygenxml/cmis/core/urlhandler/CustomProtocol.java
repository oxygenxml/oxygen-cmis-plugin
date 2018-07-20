package com.oxygenxml.cmis.core.urlhandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

/**
 * Handles the "cmis" protocol used to identify CMIS resources.
 * 
 *
 */
public class CustomProtocol extends URLStreamHandler {
  
  public static final String CMIS_PROTOCOL = "cmis";
  private static final String REPOSITORY_PARAM = "repo";
  private static final String OBJECT_ID_PARAM = "objID";
  private static final String PROTOCOL_PARAM = "proto";
  private static final String CMISOBJECT_PARAM = "type";
 
  /**
   * TODO Code review. Let's move this method inside CustomProtocol and make it reuse teh PROTOCOL, REPOSITORY constants.
   * 
   * TODO Code review. In the URL, let's put the file name as well. Oxygen will presented on the editor tab:
   * cmis:localhost:8080/atom11/file.xml?......
   * 
   * TODO Code review. When building and extracting, we should handle this case as well. THis is the AtomPub URL for Sharepoint:
   * 
   * http://<host>/_vti_bin/cmis/rest?getRepositories
   * 
   * It already has something inside GET. We must be able to recreate the URL as it was.
   * 
   * @param object
   * @param _ctrl
   * @return
   */
  public static String generateURLObject(CmisObject object, ResourceController _ctrl) {
    ResourceController ctrl = _ctrl;
    
    StringBuilder urlb = new StringBuilder();
    
    String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);
    String protocol = originalProtocol.substring(0, originalProtocol.indexOf("://"));
    
    originalProtocol = originalProtocol.replace(protocol, CMIS_PROTOCOL);
    
    urlb.append(originalProtocol).append("/");
    
    ItemIterable<QueryResult> q = null;
    
    if(object instanceof Document) {
      DocumentImpl docImpl = new DocumentImpl((Document) object);
      q = docImpl.getQuery(ctrl);
    }
    
    if(object instanceof Folder) {
      FolderImpl foldImpl = new FolderImpl((Folder) object);
      q = foldImpl.getQuery(ctrl);
    }
    
    String objectTypeId = null;
    String mimeType = null;
    
    for(QueryResult qr : q) {
     objectTypeId = (String) qr.getPropertyByQueryName("cmis:objectTypeId").getFirstValue();
    
     if(object instanceof Document) {
       mimeType = (String) qr.getPropertyByQueryName("cmis:contentStreamMimeType").getFirstValue();
       mimeType = mimeType.substring(0, mimeType.indexOf("/"));
       urlb.append(object.getName()).append("." + mimeType);
     } else {
       urlb.append(object.getName());
     }
    }
    
    urlb.append("?" + REPOSITORY_PARAM + "=").append(ctrl.getSession()
        .getSessionParameters()
        .get(SessionParameter.REPOSITORY_ID));
    urlb.append("&" + OBJECT_ID_PARAM + "=").append(object.getId());
    urlb.append("&" + PROTOCOL_PARAM + "=").append(protocol);
    urlb.append("#").append(objectTypeId);
    
    return urlb.toString();
  }
  
  
  /**
  * Gets the CmisObject identified by the given URL.
  * 
  * @param url URL identifying a CMIS resource.
  * 
  * @return The CMIS object identified by the custom URL.
  * 
  * @throws MalformedURLException If the URL doesn't contain the expected syntax.
  */
  public CmisObject getCMISObject(String url) throws MalformedURLException {
    // TODO Code review: Let's extract some constants. PROTOCOL, REPOSITORY
    // TODO Refactoring. This method and getDocumentContent() have duplicate code about tokenizing and interpreting the URL. 
    // We can extract a method that makes this, and returns an object that Contains: the server URL, the Repository ID and the Object ID.
  
    Map<String, String> params = getQueryParams(url);
    
    URL serverURL = getServerURL(url, params);
    
    String repoID= params.get(REPOSITORY_PARAM);
    if (repoID == null) {
      throw new MalformedURLException("Mising repository ID inside: " + url);
    }
    
    CMISAccess.getInstance().connect(serverURL, repoID);
    ResourceController ctrl = CMISAccess
        .getInstance().createResourceController();
    
    String objectID = params.get(OBJECT_ID_PARAM);
    if (objectID == null) {
      throw new MalformedURLException("Mising object ID inside: " + url);
    }
    
    return ctrl.getCmisObj(objectID);
  }
  

  /**
   * Using URL get cmis:document from server and return its content
   * @param url
   * @return InputReader of cmis:document
   * @throws UnsupportedEncodingException
   * @throws MalformedURLException
   */
  public Reader getDocumentContent(String url, ResourceController ctrl) throws UnsupportedEncodingException, MalformedURLException {
    
    Map<String, String> params = getQueryParams(url);
   
    String objectTypeId = params.get(CMISOBJECT_PARAM);
    String objectID = params.get(OBJECT_ID_PARAM);
    if (objectID == null) {
      throw new MalformedURLException("Mising object ID inside: " + url);
    }
    
    if(!objectTypeId.equals("cmis:document")) {
      return null;
    } 
   
    return ctrl.getDocumentContent(objectID);
  }
  
  /**
   * Builder server URL form given custom URL.
   * @param customURL
   * @param queryParams
   * @return
   * @throws MalformedURLException
   */
  public URL getServerURL(String customURL, Map<String, String> queryParams) throws MalformedURLException {
    String protocol = queryParams.get(PROTOCOL_PARAM);
    customURL = customURL.replaceFirst(customURL.substring(0, customURL.indexOf("://")), protocol);
    
    URL url = new URL(customURL);
    
    URL serverURL = new URL(
        protocol, 
        url.getHost(), 
        url.getPort(), 
        url.getPath().substring(0, url.getPath().lastIndexOf("/")));
    
    // TODO Put back the query part that isn't ours....

    return serverURL;
  }
  
  /**
   * MAP of query parameters inside given URL.
   * @param customURL
   * @return
   */
  private Map<String, String> getQueryParams(String customURL) {
    Map<String, String> params = new HashMap<>();
    
    String queryPart = customURL.substring(customURL.indexOf("?") + 1, customURL.indexOf("#"));
    String[] pairs = queryPart.split("&");
    
    for (int i = 0; i < pairs.length; i++) {
      String[] nameVal = pairs[i].split("=");
      params.put(nameVal[0], nameVal.length > 1 ? nameVal[1] : null);
    }
    
    String cmisType = customURL.substring(customURL.indexOf("#") + 1, customURL.length());
    params.put(CMISOBJECT_PARAM, cmisType);
    
    return params;
  }


  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    return new CMISURLConnection(u);
  }
  
  /**
   * Connection to a CMIS Server.
   */
  private class CMISURLConnection extends URLConnection {

    protected CMISURLConnection(URL url) {
      super(url);
    }

    @Override
    public void connect() throws IOException {
      // Not sure if we should do something.
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
      Document document = (Document) getCMISObject(getURL().toExternalForm());
      
      return document.getContentStream().getStream();
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
      return new ByteArrayOutputStream() {
        @Override
        public void close() throws IOException {
          // All bytes have been written.
          Document document = (Document) getCMISObject(getURL().toExternalForm());
          byte[] byteArray = toByteArray();
          ContentStream contentStream = new ContentStreamImpl(
              document.getName(), 
              BigInteger.valueOf(byteArray.length), 
              document.getContentStreamMimeType(), 
              new ByteArrayInputStream(byteArray));
          
          // TODO What to do if the system created a new document.
          // TODO Maybe refresh the browser....
          Document setContentStream = document.setContentStream(contentStream, true);
        }
      };
    }
    
  }
}
