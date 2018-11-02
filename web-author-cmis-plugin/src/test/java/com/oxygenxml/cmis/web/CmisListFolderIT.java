package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ro.sync.net.protocol.FolderEntryDescriptor;

public class CmisListFolderIT {
  @Rule
  public CmisAccessProvider cmisAccessProvider = new CmisAccessProvider();
	private CmisBrowsingURLConnection browsing;

	@Before
	public void setUp() throws Exception {
		URL url = new URL("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/My_Folder-0-0");
		browsing = new CmisBrowsingURLConnection(cmisAccessProvider.createConnection(url), url);
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
}
