package com.oxygenxml.cmis.core.urlhandler;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import com.oxygenxml.cmis.core.ResourceController;

public class CustomProtocolExtension {

  /**
   * 
   * @param object
   * @param ctrl
   * @return
   */
  public String getCustomURL(CmisObject object, ResourceController ctrl) {
    if(object == null || ctrl == null) {
      throw new NullPointerException();
    }
    return new CustomProtocol().generateURLObject(object, ctrl);
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
    return new CustomProtocol().getCMISObject(url);
  }
  
  /**
   * Helper methods to get content from cmis:document using URL
   * @param url
   * @return
   * @throws UnsupportedEncodingException
   * @throws MalformedURLException
   */
  public Reader getContentURL(String url, ResourceController ctrl) throws UnsupportedEncodingException, MalformedURLException {
    if(url == null) {
      throw new NullPointerException();
    }
    return new CustomProtocol().getDocumentContent(url, ctrl);
  }
}
