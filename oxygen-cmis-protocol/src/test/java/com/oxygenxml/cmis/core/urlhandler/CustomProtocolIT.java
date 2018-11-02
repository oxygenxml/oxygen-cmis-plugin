package com.oxygenxml.cmis.core.urlhandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ConnectionTestBase;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;

/**
 * Tests for accessing CMIS resources through our custom protocol.
 */
public class CustomProtocolIT extends ConnectionTestBase {

	/**
	 * TODO Code review. It makes no sense to keep the document as a member variable
	 * since it is used only in each test. A local variable in each test would
	 * suffice. A member variable makes sense if we always create it on the setUp()
	 * and we delete it on the afterMethod().
	 */
	private Folder root;
	private ResourceController ctrl;
	private String serverUrl = "http://localhost:8080/B/atom11";

	/**
	 * CONECTION TO SERVER REPOSITORY, ACCES ROOT FOLDER
	 * 
	 * @throws MalformedURLException
	 */
	@Before
	public void setUp() throws MalformedURLException {
		CMISAccess.getInstance().connectToRepo(new URL("http://localhost:8080/B/atom11"), "A1",
				new UserCredentials("admin", "admin"));
		ctrl = CMISAccess.getInstance().createResourceController();
		root = ctrl.getRootFolder();
	}

	@Test
	public void testGenerateURLObject() throws UnsupportedEncodingException {
		Document doc = ctrl.createDocument(root, "Doc", "some text", "text/plain");
		String url = CmisURLConnection.generateURLObject(doc, ctrl, "/A1/");

		assertNotNull("Document is null", doc);
		assertNotNull("URL is null", url);
		assertEquals("Invalid URL", "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/Doc", url);

		ctrl.deleteAllVersionsDocument(doc);
	}

	@Test
	public void testGetObjectFromURL() throws IOException {
		Document doc = ctrl.createDocument(root, "urlDoc1", "some text", "text/plain");
		String url = CmisURLConnection.generateURLObject(doc, ctrl, "/A1/");
		
		assertNotNull("Document is null", doc);
		assertNotNull("URL is null", url);
		assertEquals("Ivalid URL", "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/urlDoc1", url);
		
		Document docURL = (Document) getObjectFromURL(url, serverUrl, new UserCredentials("admin", "admin"));

		assertNotNull("Object is null", docURL);
		assertEquals("Invalid object name", "urlDoc1", docURL.getName());
		
		ctrl.deleteAllVersionsDocument(doc);
	}

	@Test
	public void testGetFolderFromURL() throws IOException {
		Folder folder = (Folder) ctrl.getSession().getObjectByPath("/My_Folder-0-1/My_Folder-1-0");
		String url = CmisURLConnection.generateURLObject(folder, ctrl, "/");
		
		assertNotNull("Folder is null", folder);
		assertNotNull("URL is null", url);
		assertEquals("Invalid URL", "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Folder-0-1/My_Folder-1-0", url);
		
		Folder foldURL = (Folder) getObjectFromURL(url, serverUrl, new UserCredentials("admin", "admin"));

		assertNotNull("Null folder", foldURL);
	}

	@After
	public void afterMethod() {
		cleanUpDocuments();
		cleanUpFolders();
		ctrl.getSession().clear();
	}
}
