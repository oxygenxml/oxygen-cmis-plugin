package com.oxygenxml.cmis.core.urlhandler;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.CmisObject;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;

public class CustomProtocol{
 
  /**
  * Method which use custom URL for connecting and takes from server CmisObject
  * @param url
  * @return CmisObject using ObjectID from custom URL
  * @throws MalformedURLException
  */
  public CmisObject getObjectFromURL(String url) throws MalformedURLException {
    // TODO Code review: Let's extract some constants. PROTOCOL, REPOSITORY
    // TODO Refactoring. This method and getDocumentContent() have duplicate code about tokenizing and interpreting the URL. 
    // We can extract a method that makes this, and returns an object that Contains: the server URL, the Repository ID and the Object ID.
    String protocol = url
        .substring((url.indexOf("proto=") + "proto=".length()), url.indexOf("#"));
   
    String serverURL = url
        .substring(0, url.indexOf("?")).replace(url
        .substring(0, url.indexOf("://")), protocol);
    
    String repoID = url
        .substring((url.indexOf("repo=") + "repo=".length()), url.indexOf("&"));
    
    CMISAccess.getInstance().connect(new URL(serverURL), repoID);
    
    ResourceController ctrl = CMISAccess
        .getInstance().createResourceController();
    
    String objectID = url
        .substring((url.indexOf("objID=") + "objID=".length()), url.lastIndexOf("&"));

    return ctrl.getCmisObj(objectID);
  }
  
  /**
   * Using URL get cmis:document from server and return its content
   * @param url
   * @return InputReader of cmis:document
   * @throws UnsupportedEncodingException
   * @throws MalformedURLException
   */
  public Reader getDocumentContent(String url) throws UnsupportedEncodingException, MalformedURLException {
    String objectTypeId = url.substring((url.indexOf("#") + 1), url.length());
    
    if(!objectTypeId.equals("cmis:document")) {
      return null;
    }
    
    String protocol = url
        .substring((url.indexOf("proto=") + "proto=".length()), url.indexOf("#"));
   
    String serverURL = url
        .substring(0, url.indexOf("?")).replace(url
        .substring(0, url.indexOf("://")), protocol);
    
    String repoID = url
        .substring((url.indexOf("repo=") + "repo=".length()), url.indexOf("&"));
    
    CMISAccess.getInstance().connect(new URL(serverURL), repoID);
    
    ResourceController ctrl = CMISAccess
        .getInstance().createResourceController();
    
    String objectID = url
        .substring((url.indexOf("objID=") + "objID=".length()), url.lastIndexOf("&"));
   
    return ctrl.getDocumentContent(objectID);
  }
}
