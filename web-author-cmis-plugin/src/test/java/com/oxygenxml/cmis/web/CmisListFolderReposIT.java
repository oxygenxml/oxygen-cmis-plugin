package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.junit.Rule;
import org.junit.Test;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.net.protocol.FolderEntryDescriptor;

public class CmisListFolderReposIT {
  @Rule
  public CmisAccessProvider cmisAccessProvider = new CmisAccessProvider();
  
	@Test
	public void testListFolderMethod()
			throws CmisUnauthorizedException, MalformedURLException, UnsupportedEncodingException {
		String testRepo = "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/";

    CmisURLConnection connection = cmisAccessProvider.createConnection(new URL(testRepo));
    CmisBrowsingURLConnection browsing = new CmisBrowsingURLConnection(connection,
        new URL("http://localhost:8080/B/atom11"));
    List<FolderEntryDescriptor> list = new ArrayList<FolderEntryDescriptor>();
		browsing.rootEntryMethod(list);

		for (FolderEntryDescriptor fed : list) {
			assertTrue(fed.getAbsolutePath().equals(testRepo));
		}
	}
}
