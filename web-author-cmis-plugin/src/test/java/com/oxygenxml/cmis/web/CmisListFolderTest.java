package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.net.protocol.FolderEntryDescriptor;

public class CmisListFolderTest {
	/**
	 * Executes operations over the resources.
	 */
	private ResourceController ctrl;
	private URL serverUrl;

	@Before
	public void setUp() throws Exception {
		serverUrl = new URL("http://localhost:8080/B/atom11");

		CMISAccess.getInstance().connectToRepo(serverUrl, "A1", new UserCredentials("admin", ""));
		ctrl = CMISAccess.getInstance().createResourceController();
	}

	@Test
	public void testListFolderMethod()
			throws MalformedURLException, CmisUnauthorizedException, UnsupportedEncodingException {
		
		List<String> testList = new ArrayList<String>();
		testList.add("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Document-0-0");
		testList.add("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Document-0-1");
		testList.add("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Document-0-2");
		testList.add("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Folder-0-0/");
		testList.add("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Folder-0-1/");

		List<FolderEntryDescriptor> list = new ArrayList<FolderEntryDescriptor>();
		entryMethod(list);

		assertNotNull(list);
		assertNotNull(testList);
		
		int index = 0;
		for (FolderEntryDescriptor fed : list) {
			assertTrue(fed.getAbsolutePath().equals(testList.get(index++)));
		}
	}

	public void entryMethod(List<FolderEntryDescriptor> list)
			throws MalformedURLException, CmisUnauthorizedException, UnsupportedEncodingException {
		Folder parent = ctrl.getSession().getRootFolder();
		
		for (CmisObject obj : parent.getChildren()) {

			String parentPath = "/";
			String entryUrl = CmisURLConnection.generateURLObject(obj, ctrl, parentPath);

			if (obj instanceof Folder) {
				entryUrl = entryUrl.concat("/");
			}

			list.add(new FolderEntryDescriptor(entryUrl));
		}
	}

}
