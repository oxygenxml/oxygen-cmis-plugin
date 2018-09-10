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
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;

public class SearchControllerTest extends ConnectionTestBase {

  private ResourceController ctrl;

  @Before
  public void setUp() throws MalformedURLException {
    // CMISAccess.getInstance().connectToRepo(new
    // URL("http://localhost:8080/B/atom11"), "A1",
    // new UserCredentials("admin", "admin"));
    CMISAccess.getInstance().connectToRepo(
        new URL("http://localhost:8990/alfresco/api/-default-/cmis/versions/1.1/atom"), "-default-",
        new UserCredentials("admin", "1234"));
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

    // assertNotNull(docs);
    // Will not work on jetty
  }

  @Test
  public void testFindAllDocExceptBlocked() {
    SearchController search = new SearchController(ctrl);

    List<IResource> resources = search.queryDoc("myfile");

    assertNotNull(resources);
   

    for (IResource iResource : resources) {
      System.out.println("Name:" + ((DocumentImpl) iResource).getDisplayName());
      System.out.println("Chceckd out:" + ((DocumentImpl) iResource).isCheckedOut());
      System.out.println("PWC:" + ((DocumentImpl) iResource).isPrivateWorkingCopy());
    }

    assertNotNull(resources);
  }

  @After
  public void afterMethod() {
    ctrl.getSession().clear();
  }
}
