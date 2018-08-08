package com.oxygenxml.cmis.core.model.impl;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ConnectionTestBase;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;

public class FolderImplTest extends ConnectionTestBase {

  /**
   * Root folder.
   */
  private Folder root;
  /**
   * Executes operations over the resources.
   */
  private ResourceController ctrl;

  /**
   * CONECTION TO SERVER REPOSITORY, ACCES ROOT FOLDER
   * 
   * @throws MalformedURLException
   */
  @Before
  public void setUp() throws MalformedURLException {
    // Connect
    CMISAccess.getInstance().connectToRepo(new URL("http://localhost:8080/B/atom11"), "A1");
    ctrl = CMISAccess.getInstance().createResourceController();
    root = ctrl.getRootFolder();
  }

  /**
   * PRINTS ROOT FOLDER IERARCHY
   */
  @Test
  public void rootIerarchyTest() {
    // Set Up
    FolderImpl folderImpl = new FolderImpl(root);

    StringBuilder b = new StringBuilder();
    dump(folderImpl, b, "");

    System.out.println(b);
  }

  /**
   * PRINT IERARCHY OF ELEMENTS IN REPOSITORY helper method for
   * rootIerarchyTest()
   * 
   * @param resource
   * @param b
   * @param indent
   */
  private void dump(IResource resource, StringBuilder b, String indent) {
    b.append(indent).append(resource.getDisplayName()).append("\n");

    Iterator<IResource> iterator = resource.iterator();
    while (iterator.hasNext()) {
      IResource childResource = (IResource) iterator.next();
      dump(childResource, b, indent + "  ");
    }
  }

  @Test
  public void testGetDisplayName() throws UnsupportedEncodingException {
    // Set Up
    Folder folder = createFolder(root, "testDisplayName");
    FolderImpl testFolder = new FolderImpl(folder);

    System.out.println("Folder name: " + testFolder.getDisplayName());
    assertEquals("testDisplayName", testFolder.getDisplayName());

  }

  @Test
  public void testGetId() {
    SearchController search = new SearchController(ctrl);
    List<IResource> list = search.queringFolder("My_Folder-1-0");

    IFolder fold = (IFolder) list.get(0);

    System.out.println("Folder ID: " + fold.getId());
    assertEquals("118", fold.getId());

  }

  @Test
  public void testGetFolderPath() throws UnsupportedEncodingException {
    // Set Up
    Folder folder = createFolder(root, "testFolderPath");
    FolderImpl testFolder = new FolderImpl(folder);
    
    System.out.println("id " + testFolder.getFolder().getParentId());
    System.out.println("pa " + testFolder.getFolder().getParents());

    System.out.println(testFolder.getDisplayName() + " -path-> " + testFolder.getFolderPath());

    assertEquals("/testFolderPath", testFolder.getFolderPath());

  }

  /**
   * GET GUERY OF OBJECT
   * 
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testGetQuery() throws UnsupportedEncodingException {
    Folder folder = createFolder(root, "query folder");
    FolderImpl fold = new FolderImpl(folder);

    ItemIterable<QueryResult> q = fold.getQuery(ctrl);

    for (QueryResult qr : q) {
      System.out.println("------------------------------------------\n"
          + qr.getPropertyByQueryName("cmis:objectTypeId").getFirstValue() + " , "
          + qr.getPropertyByQueryName("cmis:name").getFirstValue() + " , "
          + qr.getPropertyByQueryName("cmis:createdBy").getFirstValue());
    }

  }

  @After
  public void afterMethod() {
    cleanUpFolders();
    ctrl.getSession().clear();
  }
}
