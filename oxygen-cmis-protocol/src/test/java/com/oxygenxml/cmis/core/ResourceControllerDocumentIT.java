package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for operations over resources.
 * 
 */
public class ResourceControllerDocumentIT extends ConnectionTestBase {
	/**
	 * Root folder.
	 */
	private Folder testFolder;
	/**
	 * Executes operations over the resources.
	 */
	private ResourceController ctrl;

	@Before
	public void setUp() throws Exception {
		CMISAccess.getInstance().connectToRepo(new URL("http://localhost:8080/B/atom11"), "A1",
				new UserCredentials("admin", "admin"));

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.NAME, "testFolder");
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");

		ctrl = CMISAccess.getInstance().createResourceController();

		// Create a folder to keep the new documents.
		testFolder = ctrl.createFolder(ctrl.getRootFolder(), "docTestFolder");
	}

	/**
	 * Tests the deletion of a document.
	 * 
	 * @throws IOException
	 *             If it fails.
	 */
	@Test
	public void testDocumentDelete() throws IOException {
		Document document = ctrl.createDocument(testFolder, "test1.txt", "test content", "text/plain");

		ctrl.deleteAllVersionsDocument(document);
		document = null;

		assertFalse("The file wasn't deleted", documentExists(document, testFolder));
		assertNull("Document isn't null" ,document);
	}

	@Test
	public void testMoveDocument() throws IOException {
		Folder sourceFolder = testFolder;
		Folder targetFolder = ctrl.createFolder(sourceFolder, "testFolderMove");
		Document document = ctrl.createDocument(testFolder, "test1.txt", "test content", "text/plain");

		ctrl.move(sourceFolder, targetFolder, document);

		assertTrue("The file wasn't moved", documentExists(document, targetFolder));
		assertNotNull("Document is null", document);
		assertNotNull("Source folder is null", sourceFolder);
		assertNotNull("Target folder is null", targetFolder);
		
		ctrl.deleteFolderTree(targetFolder);
	}

	@Test
	public void testDocumentContent() throws IOException {
		Document doc = ctrl.createDocument(testFolder, "contentDoc.doc", "some test text", "text/plain");
		Reader docContent = ctrl.getDocumentContent(doc.getId());
		
		assertNotNull("Document is null", doc);
		assertNotNull("Reader is null", docContent);
		assertEquals("some test text", read(docContent));
		
		ctrl.deleteAllVersionsDocument(doc);
	}

	@After
	public void afterMethod() {
		if (testFolder != null) {
			ctrl.deleteFolderTree(testFolder);
			ctrl.getSession().clear();
		}
	}
}
