package com.oxygenxml.cmis.core.urlhandler;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.commons.SessionParameter;

import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;

public class CustomProtocolExtension {

  /**
   * TODO Code review. Let's move this method inside CustomProtocol and make it reuse teh PROTOCOL, REPOSITORY constants.
   * 
   * TODO Code reciew. In the URL, let's put the file name as well. Oxygen will presented on the editor tab:
   * cmis:localhost:8080/atom11/file.xml?......
   * 
   * TODO Code review. When building and extracting, we should hanle this case as well. THis is the AtomPub URL for Sharepoint:
   * 
   * http://<host>/_vti_bin/cmis/rest?getRepositories
   * 
   * It already has something inside GET. We must be able to recreate the URL as it was.
   * 
   * @param object
   * @param _ctrl
   * @return
   */
  public String generateURLObject(CmisObject object, ResourceController _ctrl) {
    ResourceController ctrl = _ctrl;
    
    StringBuilder urlb = new StringBuilder();
    
    String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);
    String protocol = originalProtocol.substring(0, originalProtocol.indexOf("://"));
    
    originalProtocol = originalProtocol.replace(protocol, "cmis");
    
    urlb.append(originalProtocol).append("/");
    
    DocumentImpl docImpl = new DocumentImpl((Document) object);
    ItemIterable<QueryResult> q = docImpl.getQuery(ctrl);
    
    String objectTypeId = null;
    
    for(QueryResult qr : q) {
     objectTypeId = (String) qr.getPropertyByQueryName("cmis:objectTypeId").getFirstValue();
    }
    
    urlb.append("?repo=").append(ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID));
    urlb.append("&objID=").append(object.getId());
    urlb.append("&proto=").append(protocol);
    urlb.append("#").append(objectTypeId);
    
    return urlb.toString();
  }
  
  /**
   * Helper method to get CmisObject from URL
   * @param url
   * @return
   * @throws MalformedURLException
   */
  public CmisObject getObjectFromURL(String url) throws MalformedURLException {
    if(url == null) {
      throw new NullPointerException();
    }
    return new CustomProtocol().getObjectFromURL(url);
  }
  
  /**
   * Helper methods to get content from cmis:document using URL
   * @param url
   * @return
   * @throws UnsupportedEncodingException
   * @throws MalformedURLException
   */
  public Reader getContentURL(String url) throws UnsupportedEncodingException, MalformedURLException {
    if(url == null) {
      throw new NullPointerException();
    }
    return new CustomProtocol().getDocumentContent(url);
  }
}
