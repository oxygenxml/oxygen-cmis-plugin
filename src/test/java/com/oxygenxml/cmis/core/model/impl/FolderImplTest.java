package com.oxygenxml.cmis.core.model.impl;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;

public class FolderImplTest {

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
   * @throws MalformedURLException
   */
  @Before
  public void setUp() throws MalformedURLException {
    CMISAccess.getInstance().connect(new URL("http://localhost:8080/B/atom11"), "A1");
    ctrl = CMISAccess.getInstance().createResourceController();
    root = ctrl.getRootFolder();
  }
  
  /**
   * PRINTS ROOT FOLDER IERARCHY
   */
  @Test
  public void rootIerarchyTest() {
    FolderImpl folderImpl = new FolderImpl(root);

    StringBuilder b = new StringBuilder();
    dump(folderImpl, b , "");
    
    System.out.println(b);
  }
  
  
  /**
   * PRINT IERARCHY OF ELEMENTS IN REPOSITORY
   * helper method for rootIerarchyTest()
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
  public void gettersTest() {
    Folder folder = ctrl.createFolder(root, "testGetters");
    FolderImpl testFolder = new FolderImpl(folder);
    
    System.out.println(testFolder.getDisplayName() + " " + testFolder.getId());
    assertEquals("testGetters", testFolder.getDisplayName());
    assertEquals("136", testFolder.getId());
    ctrl.deleteFolderTree(folder);
  }
  
  
}
































