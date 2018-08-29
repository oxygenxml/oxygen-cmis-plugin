package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.action.CmisCheckInAction;
import com.oxygenxml.cmis.web.action.CmisCheckOutAction;
import com.oxygenxml.cmis.web.action.ListOldVersionsAction;

public class ListOldVersionsActionTest {

	/**
	 * Executes operations over the resources.
	 */
	private CMISAccess cmisAccess;
	private URL serverUrl;
	private ResourceController ctrl;
	private CmisURLConnection connection;

	@Before
	public void setUp() throws Exception {
		serverUrl = new URL("http://localhost:8080/B/atom11");

		cmisAccess = new CMISAccess();
		cmisAccess.connectToRepo(serverUrl, "A1", new UserCredentials("admin", ""));
		ctrl = cmisAccess.createResourceController();

		connection = new CmisURLConnection(serverUrl, cmisAccess, new UserCredentials("admin", ""));
	}

	@Test
	public void testListOldVersions() throws Exception {
		Document document = ctrl.createVersionedDocument(ctrl.getRootFolder(), "checkout", "empty", "plain/xml",
				"VersionableType", VersioningState.MINOR);

		try {
			CmisCheckOutAction.checkOutDocument(document);

			assertNotNull(document);
			assertTrue(document.isVersionable());

			document = document.getObjectOfLatestVersion(false);
			CmisCheckInAction.checkInDocument(document, connection, "major", "");

			document = document.getObjectOfLatestVersion(false);
			assertFalse(document.isVersionSeriesCheckedOut());

			String url = CmisURLConnection.generateURLObject(document, ctrl, "/");

			assertNotNull(url);
			assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/checkout", url);

			String test = ListOldVersionsAction.listOldVersions(document, url);

			assertNotNull(test);
			assertTrue(test.startsWith("{\"checkout 1.0\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Floca"
					+ "lhost%253A8080%252FB%252Fatom11%2FA1%2Fcheckout?oldversion="));

		} finally {
			ctrl.deleteAllVersionsDocument(document);
		}
	}
}
