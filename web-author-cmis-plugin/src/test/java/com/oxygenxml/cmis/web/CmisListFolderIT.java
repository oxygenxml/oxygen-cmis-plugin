package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.basic.util.URLStreamHandlerFactorySetter;
import ro.sync.net.protocol.FolderEntryDescriptor;

public class CmisListFolderIT {
	private URL serverUrl;
	private CMISAccess cmisAccess;
	private CmisBrowsingURLConnection browsing;
	private URLStreamHandlerFactorySetter setter;

	@Before
	public void setUp() throws Exception {
		serverUrl = new URL("http://localhost:8080/B/atom11");

		cmisAccess = new CMISAccess();
		cmisAccess.connectToRepo(serverUrl, "A1", new UserCredentials("admin", ""));

		setter = new URLStreamHandlerFactorySetter();
		setter.setHandler("cmis", new URLStreamHandler() {

			@Override
			protected URLConnection openConnection(URL u) throws IOException {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		URL url = new URL("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Folder-0-0");
		browsing = new CmisBrowsingURLConnection(new CmisURLConnection(url, cmisAccess, new UserCredentials()),
				url);
	}

	@Test
	public void testListFolderMethod()
			throws MalformedURLException, CmisUnauthorizedException, UnsupportedEncodingException {
		
		List<String> testList = new ArrayList<String>();
		testList.add("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Folder-0-0/My_Document-1-0");
		testList.add("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Folder-0-0/My_Document-1-1");
		testList.add("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Folder-0-0/My_Document-1-2");
		testList.add("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Folder-0-0/My_Folder-1-0/");
		testList.add("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Folder-0-0/My_Folder-1-1/");

		List<FolderEntryDescriptor> list = new ArrayList<FolderEntryDescriptor>();
		browsing.entryMethod(list);

		assertNotNull(browsing);
		assertNotNull(list);
		assertNotNull(testList);

		int index = 0;
		for (FolderEntryDescriptor fed : list) {
			assertNotNull(fed);
			assertEquals(fed.getAbsolutePath(), testList.get(index++));
		}
	}

	@After
	public void tearDown() throws Exception {
		setter.tearDown();
	}
}
