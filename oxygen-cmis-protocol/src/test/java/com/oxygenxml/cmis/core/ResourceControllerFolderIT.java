package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * Tests for operations over resources.
 * 
 */
public class ResourceControllerFolderIT extends ConnectionTestBase {
	/**
	 * Executes operations over the resources.
	 */
	private ResourceController ctrl;
	private Folder testFolder;

	@Before
	public void setUp() throws Exception {
	  CmisAccessTestSingleton.getInstance().connectToRepo(new URL("http://localhost:8080/B/atom11"), "A1",
				new UserCredentials("admin", "admin"));

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.NAME, "testFolderResource");
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");

		ctrl = CmisAccessTestSingleton.getInstance().createResourceController();
	}

	/**
	 * Tests the creation of folder.
	 */
	@Test
	public void testPassCreateFolder() {
		testFolder = ctrl.createFolder(ctrl.getRootFolder(), "Folder Test");
		
		assertNotNull("Folder is null" ,testFolder);
		assertEquals("Invalid name" ,"Folder Test", testFolder.getName());
		
		ctrl.deleteFolderTree(testFolder);
	}

	/**
	 * 
	 */
	@Test
	public void testDeleteFolderTree() {
		testFolder = ctrl.createFolder(ctrl.getRootFolder(), "Folder Test1");
		ctrl.deleteFolderTree(testFolder);

		assertFalse("Folder wasn't deleted", folderExists(testFolder, ctrl.getRootFolder()));
	}

	/**
	 * Tests renaming a file.
	 */
	@Test
	public void testRenameFolder() {
		testFolder = ctrl.createFolder(ctrl.getRootFolder(), "Folder Test2");
		testFolder = (Folder) ctrl.renameFolder(testFolder, "Folder Renamed");
		
		assertNotNull("Folder is null", testFolder);
		assertEquals("Renaming the file failed.", "Folder Renamed", testFolder.getName());
		
		ctrl.deleteFolderTree(testFolder);
	}

	@After
	public void afterMethod() {
		cleanUpFolders();
		if (testFolder != null) {
			ctrl.getSession().clear();
		}
	}
}
