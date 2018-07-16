package com.oxygenxml.cmis.core.model.impl;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
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
  public void testGetDisplayName() throws UnsupportedEncodingException {
    // Set Up
    Document doc = ctrl.createDocument(root, "testDoc_name", "some text");
    DocumentImpl docTest = new DocumentImpl(doc);

    System.out.println("Doc name: " + docTest.getDisplayName());
    assertEquals("testDisplayNameDoc", docTest.getDisplayName());

    // Clean Up
    ctrl.deleteAllVersionsDocument(doc);
  }

  @Test
  public void testGetId() throws UnsupportedEncodingException {
    // Set Up
    Document doc = ctrl.createDocument(root, "testDisplayNameDoc", "some text");
    DocumentImpl docTest = new DocumentImpl(doc);

    System.out.println("Doc ID: " + docTest.getId());
    assertEquals("141", docTest.getId());

    // Clean Up
    ctrl.deleteAllVersionsDocument(doc);
  }

  /**
   * GET GUERY OF OBJECT
   * 
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testGetQuery() throws UnsupportedEncodingException {
    Document doc = ctrl.createDocument(root, "queryTestFile", "some text");
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

    ctrl.deleteAllVersionsDocument(doc);
  }

  @Test
  public void testGetDocumentPath() throws UnsupportedEncodingException {
    Document doc = ctrl.createDocument(root, "PathTestFile", "some text");
    DocumentImpl docImpl = new DocumentImpl(doc);

    System.out.println(docImpl.getDocumentPath(ctrl));
    assertEquals("/RootFolder/PathTestFile/", docImpl.getDocumentPath(ctrl));

    ctrl.deleteAllVersionsDocument(doc);
  }

  /*
   * Get the last version of a document
   */
  @Test
  public void testGetLastVersionDocument() throws UnsupportedEncodingException {
    Document doc = ctrl.createVersionedDocument(root, "queryTestFile", "some text",VersioningState.MINOR);

    Document latest;
    if (Boolean.TRUE.equals(doc.isLatestVersion())) {

      latest = doc;
    } else {

      latest = doc.getObjectOfLatestVersion(false);
    }
    System.out.println(latest.getName());
    System.out.println(latest.getContentStream().toString());

    ctrl.deleteAllVersionsDocument(latest);
    ctrl.deleteAllVersionsDocument(doc);
  }

  /*
   * Check is is checked-out
   */
  @Test
  public void testIsCheckedOut() throws UnsupportedEncodingException {
    Document doc = ctrl.createVersionedDocument(root, "queryTestFile", "some text",VersioningState.MINOR);

    boolean isCheckedOut = Boolean.TRUE.equals(doc.isVersionSeriesCheckedOut());
    String checkedOutBy = doc.getVersionSeriesCheckedOutBy();

    System.out.println(isCheckedOut + " checkout by " + checkedOutBy);

    ctrl.deleteAllVersionsDocument(doc);
  }

  /*
   * Check-out the document
   */
  @Test
  public void testCheckOut() throws UnsupportedEncodingException {
    Document doc = ctrl.createVersionedDocument(root, "queryTestFile", "some text",VersioningState.MINOR);
    ObjectId pwcId = doc.checkOut();
    
    System.out.println(doc.getName());
    Document pwc = (Document) CMISAccess.getInstance().getSession().getObject(pwcId);
    System.out.println(pwc.getName());

    ctrl.deleteAllVersionsDocument(doc);
    //ctrl.deleteAllVersionsDocument(pwc);
  }

}
