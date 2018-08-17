package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;

public class SearchControllerTest extends ConnectionTestBase {

  private ResourceController ctrl;

  @Before
  public void setUp() throws MalformedURLException {
    CMISAccess.getInstance().connectToRepo(new URL("http://localhost:8080/B/atom11"), "A1",
        new UserCredentials("admin", "admin"));
    ctrl = CMISAccess.getInstance().createResourceController();
  }

  @Test
  public void testQueringFolders() {
    SearchController search = new SearchController(ctrl);

    List<IResource> folds = search.queryFolderName("Folder-1-0");

    assertNotNull(folds);
  }

  @Test
  public void testQueringDoc() {
    SearchController search = new SearchController(ctrl);

    List<IResource> docs = search.queryDocName("Document");

    assertNotNull(docs);
  }

  @Test
  public void testQueringDocContent() {
    SearchController search = new SearchController(ctrl);

    List<IDocument> docs = search.queryDocContent("At justo in urna");

    assertNotNull(docs);
  }

  @Test
  public void testQueringDocAllProperties() {
    SearchController search = new SearchController(ctrl);

    List<IResource> docs = search.queryDoc("Document");

    assertNotNull(docs);
  }

  @After
  public void afterMethod() {
    ctrl.getSession().clear();
  }
}
