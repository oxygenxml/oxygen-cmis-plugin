package com.oxygenxml.cmis.core.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CmisAccessTestSingleton;
import com.oxygenxml.cmis.core.ConnectionTestBase;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;

public class DocumentImplIT extends ConnectionTestBase {

  private Folder root;
  private ResourceController ctrl;

  /**
   * CONECTION TO SERVER REPOSITORY, ACCES ROOT FOLDER
   * 
   * @throws MalformedURLException
   */
  @Before
  public void setUp() throws MalformedURLException {
    CmisAccessTestSingleton.getInstance().connectToRepo(new URL("http://localhost:8080/B/atom11"), "A1",
        new UserCredentials("admin", "admin"));
    ctrl = CmisAccessTestSingleton.getInstance().createResourceController();
    root = ctrl.getRootFolder();
  }

  /*
   * Check is is checked-out
   */
  @Test
  public void testIsCheckedOut() throws UnsupportedEncodingException {
    Document doc = createDocument(root, "que2ryTestFile", "some text");
    ObjectId pwc = doc.checkOut();
    ;
    doc.refresh();

    assertNotNull(doc);
    assertNotNull(pwc);
    assertTrue(doc.isVersionable());
    assertTrue(doc.isVersionSeriesCheckedOut());
    assertEquals("admin", doc.getVersionSeriesCheckedOutBy());

    Document PWC = (Document) CmisAccessTestSingleton.getInstance().getSession().getObject(pwc);

    assertNotNull(PWC);
    assertTrue(PWC.isPrivateWorkingCopy());

  }

  /*
   * Check in the document
   */
  @Test
  public void testCheckIn() throws UnsupportedEncodingException {
    Document doc = ctrl.createEmptyVersionedDocument(root, "queryTestFile", "plain/text",
        VersioningState.MINOR);
    ObjectId pwcId = doc.checkOut();
    Document pwc = (Document) CmisAccessTestSingleton.getInstance().getSession().getObject(pwcId);
    ObjectId idDoc = pwc.checkIn(true, null, doc.getContentStream(), "new version");

    assertNotNull(doc);
    assertNotNull(pwcId);
    assertNotNull(pwc);
    assertNotNull(idDoc);

    assertFalse(doc.isVersionSeriesCheckedOut());

    ctrl.deleteAllVersionsDocument(doc);
  }

  @Test
  public void testGetDisplayName() throws UnsupportedEncodingException {
    Document doc = ctrl.createDocument(root, "testDc", "some text", "plain/text");
    DocumentImpl docTest = new DocumentImpl(doc);

    assertNotNull(doc);
    assertNotNull(docTest);
    assertEquals("testDc", docTest.getDisplayName());

    ctrl.deleteAllVersionsDocument(doc);
  }

  @Test
  public void testGetId() throws UnsupportedEncodingException {
    SearchController search = new SearchController(ctrl);
    List<IResource> list = search.queryDocName("ment-2");
    IDocument docTest = (IDocument) list.get(0);

    assertEquals("111", docTest.getId());
    assertNotNull(search);
    assertNotNull(list);
    assertNotNull(docTest);
  }

  /**
   * GET GUERY OF OBJECT
   * 
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testGetQuery() throws UnsupportedEncodingException {
    Document doc = ctrl.createDocument(root, "queryTestFile1", "some text", "plain/text");
    DocumentImpl docImpl = new DocumentImpl(doc);

    ItemIterable<QueryResult> q = docImpl.getQuery(ctrl);

    assertNotNull(docImpl);
    assertNotNull(q);

    ctrl.deleteAllVersionsDocument(doc);
  }

  @Test
  public void testGetDocumentPath() throws UnsupportedEncodingException {
    SearchController search = new SearchController(ctrl);
    List<IResource> list = search.queryDocName("Document-2");

    IDocument doc = (IDocument) list.get(0);
    assertEquals("/RootFolder/My_Folder-0-0/My_Folder-1-1/My_Document-2-0/", doc.getDocumentPath(ctrl));
    assertNotNull(doc);
    assertNotNull(list);
    assertNotNull(ctrl);

  }

  /*
   * Get the last version of a document
   */
  @Test
  public void testGetLastVersionDocument() throws UnsupportedEncodingException {
    Document latest = null;
    Document doc = ctrl.createDocument(root, "queryTestFile2", "some text", "plain/text");

    if (Boolean.TRUE.equals(doc.isLatestVersion())) {
      latest = doc;
    } else {
      latest = doc.getObjectOfLatestVersion(false);
    }

    assertEquals("queryTestFile2", latest.getName());
    assertNotNull(latest);

    ctrl.deleteAllVersionsDocument(doc);
  }

  @After
  public void afterMethod() {
    cleanUpDocuments();
    ctrl.getSession().clear();
  }
}
