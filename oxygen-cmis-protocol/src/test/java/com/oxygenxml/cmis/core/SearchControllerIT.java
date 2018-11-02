package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;

public class SearchControllerIT extends ConnectionTestBase {

  private ResourceController ctrl;

  @Before
  public void setUp() throws MalformedURLException {
     CMISAccess.getInstance().connectToRepo(new
     URL("http://localhost:8080/B/atom11"), "A1",
     new UserCredentials("admin", "admin"));
//    CMISAccess.getInstance().connectToRepo(
//        new URL("http://localhost:8990/alfresco/api/-default-/cmis/versions/1.1/atom"), "-default-",
//        new UserCredentials("admin", "1234"));
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
 //    Will not work on jetty
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

  @Test
  public void testCaseSensitive() {
    SearchController search = new SearchController(ctrl);
    String searchKeys = "Preparation care";
    List<IResource> resources = search.queryDoc(searchKeys);

    assertNotNull(resources);

    for (IResource iResource : resources) {
      System.out.println("Name:" + ((DocumentImpl) iResource).getDisplayName());

    }

    assertNotNull(resources);
  }

  @Test
  public void testLogicOperators() {
    SearchController search = new SearchController(ctrl);
    String searchKeys = "Preparation AND care ?lower";
    List<IResource> resources = search.queryDoc(searchKeys);

    assertNotNull(resources);

    for (IResource iResource : resources) {
      System.out.println("Name:" + ((DocumentImpl) iResource).getDisplayName());

    }
  }

  @Test
  public void testGetCurrentUser() {
    SearchController search = new SearchController(ctrl);
    String searchKeys = "myfile";
    List<IResource> resources = search.queryDoc(searchKeys);
    String userName = CMISAccess.getInstance().getSession().getSessionParameters().get(SessionParameter.USER);

    for (IResource iResource : resources) {
      if (iResource.getCreatedBy().equals(userName)) {
        System.out.println("Name:" + ((DocumentImpl) iResource).getDisplayName());
      }

    }
    System.out.println("Username =" + userName);

    assertNotNull(userName);

  }

  @Test
  public void testFindCheckoutPersonalFiles() {
    SearchController search = new SearchController(ctrl);

    List<IResource> resources = search.queryPersonalCheckedout("admin");
    System.out.println("Resources=" + resources);

    for (IResource iResource : resources) {
      System.out.println("Name:" + ((DocumentImpl) iResource).getDisplayName());
      System.out.println("Chceckd out:" + ((DocumentImpl) iResource).isCheckedOut());
      System.out.println("PWC:" + ((DocumentImpl) iResource).isPrivateWorkingCopy());
      System.out.println("Versiong:" + ((DocumentImpl) iResource).getLastVersionDocument());
    }
    assertNotNull(resources);
  }
  @Test
  public void testFindForeignCHeckoutFiles() {
    SearchController search = new SearchController(ctrl);

    List<IResource> resources = search.queryForeignCheckedoutDocs("");
    System.out.println("Resources=" + resources);

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
