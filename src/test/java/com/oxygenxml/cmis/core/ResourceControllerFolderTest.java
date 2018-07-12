package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertEquals;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO Cristian More tests for the folder level actions.
 *  
 * Tests for operations over resources.
 * 
 */
public class ResourceControllerFolderTest extends ConnectionTestBase {
  /**
   * Executes operations over the resources.
   */
  private ResourceController ctrl;

  @Before
  public void setUp() throws Exception {
    CMISAccess.getInstance().connect(new URL("http://localhost:8080/B/atom11"), "A1");

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(PropertyIds.NAME, "testFolderResource");
    properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");

    ctrl = CMISAccess.getInstance().createResourceController();
  }

  /**
   * Tests the creation of folder.
   */
  @Test
  public void testPassCreateFolder(){
    Folder testFolder = ctrl.createFolder(ctrl.getRootFolder(), "testFolderCreate");
    try {
      Assert.assertFalse(folderExists(testFolder, ctrl.getRootFolder()));
      
    } finally {
      ctrl.deleteFolderTree(testFolder);
    }
  }
  
  /**
   * 
   */
  @Test
  public void testDeleteFolderTree() {
    Folder testFolder = ctrl.createFolder(ctrl.getRootFolder(), "testFolderDelete");
    ctrl.deleteFolderTree(testFolder);
    Assert.assertFalse(folderExists(testFolder, ctrl.getRootFolder()));
  }

  /**
   * Tests renaming a file.
   */
  @Test
  public void testRenameFolder() {
    Folder testFolder = ctrl.createFolder(ctrl.getRootFolder(), "testFolderRename");
    CmisObject renamedFolder = null;
    try {
      renamedFolder = ctrl.renameFolder(testFolder, "MI6");

      assertEquals("Renaming the file failed.", "MI6", testFolder.getName());
    } finally {
      if (renamedFolder != null) {
        ctrl.deleteFolderTree((Folder) renamedFolder);  
      } else {
        // The renamed probably failed. Delete the original folder.
        ctrl.deleteFolderTree(testFolder);
      }
    }
  }
  
  @After
  public void afterMethod(){
    ctrl.getSession().clear();
  }
}
