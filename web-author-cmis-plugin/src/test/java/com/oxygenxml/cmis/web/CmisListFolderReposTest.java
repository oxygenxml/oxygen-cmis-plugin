package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.net.protocol.FolderEntryDescriptor;

public class CmisListFolderReposTest {
	/**
	 * Executes operations over the resources.
	 */
	private CMISAccess cmisAccess;
	private URL serverUrl;
	private CmisBrowsingURLConnection browsing;

	@Before
	public void setUp() throws Exception {
		serverUrl = new URL("http://localhost:8080/B/atom11");

		cmisAccess = new CMISAccess();
		cmisAccess.connectToRepo(serverUrl, "A1", new UserCredentials("admin", ""));
		browsing = new CmisBrowsingURLConnection(new CmisURLConnection(serverUrl, cmisAccess, new UserCredentials()),
				serverUrl);
	}

	@Test
	public void testListFolderMethod()
			throws CmisUnauthorizedException, MalformedURLException, UnsupportedEncodingException {
		String testRepo = "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/";

		List<FolderEntryDescriptor> list = new ArrayList<FolderEntryDescriptor>();
		browsing.rootEntryMethod(list);

		assertNotNull(browsing);
		assertNotNull(testRepo);
		assertNotNull(list);

		for (FolderEntryDescriptor fed : list) {
			assertTrue(fed.getAbsolutePath().equals(testRepo));
		}
	}
}