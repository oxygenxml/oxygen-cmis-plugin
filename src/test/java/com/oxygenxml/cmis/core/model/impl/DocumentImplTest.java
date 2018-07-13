package com.oxygenxml.cmis.core.model.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ConnectionTestBase;
import com.oxygenxml.cmis.core.ResourceController;

public class DocumentImplTest extends ConnectionTestBase {

  private Folder root;
  private ResourceController ctrl;
  
  /**
   * CONECTION TO SERVER REPOSITORY, ACCES ROOT FOLDER
   * @throws MalformedURLException
   */
  @Before
  public void setUp() throws MalformedURLException {
    CMISAccess.getInstance().connect(new URL("http://localhost:8080/B/atom11"), "A1");
    ctrl = CMISAccess.getInstance().createResourceController();
    root = ctrl.getRootFolder();
  }
  
  
  /**
   * GET GUERY OF OBJECT
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testGetQuery() throws UnsupportedEncodingException {
    Document doc = ctrl.createDocument(root, "queryTestFile", "some text");
    DocumentImpl docImpl = new DocumentImpl(doc);
    
    ItemIterable<QueryResult> q = docImpl.getQuery(ctrl);
    
    int i = 1;
    for(QueryResult qr : q) {
      System.out.println("------------------------------------------\n"
          + qr.getPropertyByQueryName("cmis:objectTypeId").getFirstValue()          + " , "
          + qr.getPropertyByQueryName("cmis:name").getFirstValue()                  + " , "
          + qr.getPropertyByQueryName("cmis:createdBy").getFirstValue()             + " , "
          + qr.getPropertyByQueryName("cmis:contentStreamFileName").getFirstValue() + " , "
          + qr.getPropertyByQueryName("cmis:contentStreamMimeType").getFirstValue() + " , "
          + qr.getPropertyByQueryName("cmis:contentStreamLength").getFirstValue()          );
      i++;
    }
    
    ctrl.deleteAllVersionsDocument(doc);
    Assert.assertFalse(documentExists(doc, root));
  }

}
