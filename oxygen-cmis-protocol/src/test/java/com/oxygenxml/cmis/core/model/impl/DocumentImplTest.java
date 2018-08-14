package com.oxygenxml.cmis.core.model.impl;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ConnectionTestBase;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;

public class DocumentImplTest extends ConnectionTestBase {

  private Folder root;
  private ResourceController ctrl;

  /**
   * CONECTION TO SERVER REPOSITORY, ACCES ROOT FOLDER
   * 
   * @throws MalformedURLException
   */
  @Before
  public void setUp() throws MalformedURLException {
    CMISAccess.getInstance().connectToRepo(new URL("http://localhost:8080/B/atom11"), "A1",
        new UserCredentials("admin", "admin"));
    ctrl = CMISAccess.getInstance().createResourceController();
    root = ctrl.getRootFolder();
  }

  /*
   * Check is is checked-out
   */
  @Test
  public void testIsCheckedOut() throws UnsupportedEncodingException {
    Document doc = null;
    ObjectId pwc = null;
    doc = createDocument(root, "queryTestFile", "some text");
    pwc = doc.checkOut();
    doc.refresh();
    
    CmisObject pwcDoc = CMISAccess.getInstance().getSession().getObject(pwc);
    
    
    List<Property<?>> properties = doc.getProperties();
    for (Property<?> property : properties) {
      System.out.println(property.getId() + " -> " + property.getValues());
    }
  

    System.out.println("this is the document:" + doc.getProperty("cmis:isVersionSeriesCheckedOut").getValuesAsString());
    System.out
        .println("this is the private :" + pwcDoc.getProperty("cmis:isVersionSeriesCheckedOut").getValuesAsString());

    System.out.println("Is it checkout:" + doc.isVersionSeriesCheckedOut());


    Boolean isCheckedOut = doc.isVersionSeriesCheckedOut();
    // cmis:versionSeriesCheckedOutBy
    String checkedOutBy = doc.getVersionSeriesCheckedOutBy();

    assertEquals(true, isCheckedOut);
    System.out.println(isCheckedOut + " checkout by " + checkedOutBy);
  }

  /*
   * Check in the document
   */
  @Test
  public void testCheckIn() throws UnsupportedEncodingException {
    Document doc = null;
    doc = createDocument(root, "queryTestFile3", "some text");
    ObjectId pwcId = doc.checkOut();

    System.out.println(doc.getName());
    Document pwc = (Document) CMISAccess.getInstance().getSession().getObject(pwcId);
    System.out.println(pwc.getName());

   

    ObjectId idDoc = pwc.checkIn(true, null, doc.getContentStream(), "new version");

    System.out.println("Object checked in =" + CMISAccess.getInstance().getSession().getObject(idDoc));

  }

  @Test
  public void testGetDisplayName() throws UnsupportedEncodingException {
    Document doc = null;

    // Set Up
    doc = createDocument(root, "testDoc_name", "some text");
    DocumentImpl docTest = new DocumentImpl(doc);

    System.out.println("Doc name: " + docTest.getDisplayName());
    assertEquals("testDoc_name", docTest.getDisplayName());

  }

  @Test
  public void testGetId() throws UnsupportedEncodingException {
    SearchController search = new SearchController(ctrl);
    List<IResource> list = search.queryDocName("ment-2");

    IDocument docTest = (IDocument) list.get(0);

    System.out.println("Doc ID: " + docTest.getId());
    assertEquals("111", docTest.getId());

  }

  /**
   * GET GUERY OF OBJECT
   * 
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testGetQuery() throws UnsupportedEncodingException {
    Document doc = null;

    doc = createDocument(root, "queryTestFile1", "some text");
    DocumentImpl docImpl = new DocumentImpl(doc);

    ItemIterable<QueryResult> q = docImpl.getQuery(ctrl);

    for (QueryResult qr : q) {
      System.out.println("------------------------------------------\n"
          + qr.getPropertyByQueryName("cmis:objectTypeId").getFirstValue() + " , "
          + qr.getPropertyByQueryName("cmis:name").getFirstValue() + " , "
          + qr.getPropertyByQueryName("cmis:createdBy").getFirstValue() + " , "
          + qr.getPropertyByQueryName("cmis:contentStreamFileName").getFirstValue() + " , "
          + qr.getPropertyByQueryName("cmis:contentStreamMimeType").getFirstValue() + " , "
          + qr.getPropertyByQueryName("cmis:contentStreamLength").getFirstValue());
    }
  }

  @Test
  public void testGetDocumentPath() throws UnsupportedEncodingException {

    SearchController search = new SearchController(ctrl);
    List<IResource> list = search.queryDocName("Document-2");

    IDocument doc = (IDocument) list.get(0);
    System.out.println(doc.getDocumentPath(ctrl));
    assertEquals("/RootFolder/My_Folder-0-0/My_Folder-1-1/My_Document-2-0/", doc.getDocumentPath(ctrl));

  }

  /*
   * Get the last version of a document
   */
  @Test
  public void testGetLastVersionDocument() throws UnsupportedEncodingException {
    Document latest = null;
    Document doc = createDocument(root, "queryTestFile2", "some text");

    if (Boolean.TRUE.equals(doc.isLatestVersion())) {

      latest = doc;
    } else {

      latest = doc.getObjectOfLatestVersion(false);
    }
    System.out.println(latest.getName());
    System.out.println(latest.getContentStream().toString());
  }

  @After
  public void afterMethod() {
    cleanUpDocuments();
    ctrl.getSession().clear();
  }
}
