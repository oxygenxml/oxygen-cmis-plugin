package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
		String repoUrl = "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/Apache%20Chemistry%20OpenCMIS%20InMemory%20Repository%20%5BA1%5D/";
		

    CmisURLConnection connection = cmisAccessProvider.createConnection(new URL(repoUrl));
    CmisBrowsingURLConnection browsing = new CmisBrowsingURLConnection(connection,
        new URL("http://localhost:8080/B/atom11"));
    List<FolderEntryDescriptor> list = browsing.getRootFolderEntriesDescriptiors();

		for (FolderEntryDescriptor fed : list) {
			assertEquals(repoUrl, fed.getAbsolutePath());
		}
	}
	
	
}
