package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.commons.SessionParameter;
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

	@Before
	public void setUp() throws Exception {
		serverUrl = new URL("http://localhost:8080/B/atom11");

		cmisAccess = new CMISAccess();
		cmisAccess.connectToRepo(serverUrl, "A1", new UserCredentials("admin", ""));
	}

	@Test
	public void testListFolderMethod()
			throws CmisUnauthorizedException, MalformedURLException, UnsupportedEncodingException {
		String testRepo = "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/";

		List<FolderEntryDescriptor> list = new ArrayList<FolderEntryDescriptor>();
		rootEntryMethod(list);

		assertNotNull(testRepo);
		assertNotNull(list);

		for (FolderEntryDescriptor fed : list) {
			assertTrue(fed.getAbsolutePath().equals(testRepo));
		}

	}

	public void rootEntryMethod(List<FolderEntryDescriptor> list)
			throws MalformedURLException, UnsupportedEncodingException, CmisUnauthorizedException {
		List<Repository> reposList = cmisAccess.connectToServerGetRepositories(serverUrl,
				new UserCredentials("admin", ""));

		for (Repository repos : reposList) {
			String reposUrl = generateRepoUrl(repos);
			list.add(new FolderEntryDescriptor(reposUrl));
		}
	}

	public String generateRepoUrl(Repository repo)
			throws UnsupportedEncodingException, MalformedURLException, CmisUnauthorizedException {
		StringBuilder urlb = new StringBuilder();
		// Connecting to Server to get host
		cmisAccess.connectToRepo(serverUrl, repo.getId(), new UserCredentials("admin", ""));
		// Get server URL
		String originalProtocol = cmisAccess.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);

		originalProtocol = URLEncoder.encode(originalProtocol, "UTF-8");
		urlb.append((CmisURLConnection.CMIS_PROTOCOL + "://")).append(originalProtocol).append("/");
		urlb.append(repo.getId()).append("/");

		return urlb.toString();
	}
}
