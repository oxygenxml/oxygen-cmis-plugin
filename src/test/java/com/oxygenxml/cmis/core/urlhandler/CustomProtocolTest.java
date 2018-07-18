package com.oxygenxml.cmis.core.urlhandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ConnectionTestBase;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.urlhandler.CustomProtocolExtension;

public class CustomProtocolTest extends ConnectionTestBase {

  private Document doc;
  private Folder root;
  private ResourceController ctrl;

  /**
   * CONECTION TO SERVER REPOSITORY, ACCES ROOT FOLDER
   * 
   * @throws MalformedURLException
   */
  @Before
  public void setUp() throws MalformedURLException {
    CMISAccess.getInstance().connect(new URL("http://localhost:8080/B/atom11"), "A1");
    ctrl = CMISAccess.getInstance().createResourceController();
    root = ctrl.getRootFolder();
  }
  
  @Test
  public void testGenerateURLObject() throws UnsupportedEncodingException {
    doc = ctrl.createDocument(root, "urlDoc", "some text");
    
    CustomProtocolExtension cpe = new CustomProtocolExtension();
    String url = cpe.generateURLObject(doc, ctrl);
    
    System.out.println("[cmis:document] id = " + doc.getId() + " URL = " + url);
    
    assertNotNull(url);
    
    ctrl.deleteAllVersionsDocument(doc);
  }

  @Test
  public void testGetObjectFromURL() throws UnsupportedEncodingException, MalformedURLException {
    doc = ctrl.createDocument(root, "urlDocGet", "some text");
  
    CustomProtocolExtension cpe = new CustomProtocolExtension();
    String url = cpe.generateURLObject(doc, ctrl);
    
    System.out.println("[cmis:document] id = " + doc.getId() + " URL = " + url);
    
    Document docURL = (Document) cpe.getObjectFromURL(url);
    
    assertNotNull(docURL);
    assertEquals("urlDocGet", docURL.getName());
    
    ctrl.deleteAllVersionsDocument(doc);
  }
  
  @Test
  public void testGetDocumentContent() throws IOException, UnsupportedEncodingException, MalformedURLException {
    doc = ctrl.createDocument(root, "urlDocCont", "some test text");
  
    CustomProtocolExtension cpe = new CustomProtocolExtension();
    String url = cpe.generateURLObject(doc, ctrl);
    
    System.out.println("[cmis:document] id = " + doc.getId() + " URL = " + url);
    
    Reader docContent = cpe.getContentURL(url);
    
    assertNotNull(docContent);
    assertEquals("some test text", read(docContent));
    
    ctrl.deleteAllVersionsDocument(doc);
  }
  
  @After
  public void afterMethod(){   
    ctrl.getSession().clear();
  }
}
