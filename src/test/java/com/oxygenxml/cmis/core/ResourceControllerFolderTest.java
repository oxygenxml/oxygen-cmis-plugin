package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertEquals;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
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
    CMISAccess.getInstance().connect(new URL("http://localhost:8080/atom11"), "A1");

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(PropertyIds.NAME, "testFolder");
    properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");

    ctrl = CMISAccess.getInstance().createResourceController();
  }

  /**
   * Tests the creation of folder.
   */
  @Test
  public void testPassCreateFolder(){
    Folder testFolder = ctrl.createFolder(ctrl.getRootFolder(), "testFolder");
    try {
      assertEquals("testFolder", testFolder.getName());
      
    } finally {
      ctrl.deleteFolderTree(testFolder);
    }
  }
  
  /**
   * IN DEVELOPMENT
   */
  @Test
  public void testDeleteFolderTree() {
    
  }

  /**
   * Tests renaming a file.
   */
  @Test
  public void testRenameFolder() {
    Folder testFolder = ctrl.createFolder(ctrl.getRootFolder(), "testFolder");
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
}
