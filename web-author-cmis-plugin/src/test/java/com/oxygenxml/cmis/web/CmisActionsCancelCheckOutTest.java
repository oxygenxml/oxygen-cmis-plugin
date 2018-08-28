package com.oxygenxml.cmis.web;

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
import com.oxygenxml.cmis.web.action.CmisCheckOutAction;

public class CmisActionsCancelCheckOutTest {
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
	public void testCancelCheckOut() throws Exception {
		Document document = ctrl.createVersionedDocument(ctrl.getRootFolder(), "cancel", "empty", "plain/xml",
				"VersionableType", VersioningState.MINOR);

		try {
			CmisCheckOutAction.checkOutDocument(document);

			assertNotNull(document);
			assertTrue(document.isVersionable());

			document = document.getObjectOfLatestVersion(false);
			assertTrue(document.isVersionSeriesCheckedOut());

			CmisCheckOutAction.cancelCheckOutDocument(document, connection);

			document = document.getObjectOfLatestVersion(false);
			assertFalse(document.isVersionSeriesCheckedOut());
			
		} finally {
			ctrl.deleteAllVersionsDocument(document);
		}
	}
}
