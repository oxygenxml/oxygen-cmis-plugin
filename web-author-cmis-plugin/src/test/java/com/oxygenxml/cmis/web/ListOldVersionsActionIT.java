package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.action.CmisCancelCheckOut;
import com.oxygenxml.cmis.web.action.CmisCheckIn;
import com.oxygenxml.cmis.web.action.CmisCheckOut;
import com.oxygenxml.cmis.web.action.CmisOldVersions;

public class ListOldVersionsActionIT {

  @Rule
  public CmisAccessProvider cmisAccessProvider = new CmisAccessProvider();

	private ResourceController ctrl;

	@Before
	public void setUp() throws Exception {
	  CMISAccess cmisAccess = cmisAccessProvider.getCmisAccess();
		ctrl = cmisAccess.createResourceController();
	}
	
	
	@Test
	public void testOneVersionOfDocument() throws Exception {
		Document document = null;
		
		try {
			document = ctrl.createEmptyVersionedDocument(ctrl.getRootFolder(), "oneVersion", 
					ResourceController.VERSIONABLE_OBJ_TYPE, VersioningState.MINOR);
			
			if (document.isVersionSeriesCheckedOut()) {
				CmisCancelCheckOut.cancelCheckOutDocument(document, ctrl.getSession());
			}
			
			String url = CmisURLConnection.generateURLObject(document, ctrl, "/");
			String test = CmisOldVersions.listOldVersions(document, url, "current");
			
			assertTrue(test, test
				.equals("{\"v0.1\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252"
						+ "Fatom11%2FA1%2FoneVersion\",\"\",\"admin\"]}"));
			
		} finally {
			if (document != null) {
				ctrl.deleteAllVersionsDocument(document);
			}
		}
	}
	
	
	@Test
	public void testNoCheckOutMajorDocument() throws Exception {
		Document document = null;
		
		try {
			document = ctrl.createEmptyVersionedDocument(ctrl.getRootFolder(), "checkedOutMajor", 
					ResourceController.VERSIONABLE_OBJ_TYPE, VersioningState.MINOR);
			
			if (document.isVersionSeriesCheckedOut()) {
				CmisCancelCheckOut.cancelCheckOutDocument(document, ctrl.getSession());
			}
			
			document = document.getObjectOfLatestVersion(false);
			CmisCheckOut.checkOutDocument(document);
			
			document = document.getObjectOfLatestVersion(false);
			CmisCheckIn.checkInDocument(document, ctrl.getSession(), "major", "some commit");

			String url = CmisURLConnection.generateURLObject(document, ctrl, "/");
			String test = CmisOldVersions.listOldVersions(document, url, "current");
			
			assertTrue(test, test
				.startsWith("{\"v1.0\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252"
						+ "Fatom11%2FA1%2FcheckedOutMajor\",\"some commit\",\"admin\"]"));
			
		} finally {
			if (document != null) {
				ctrl.deleteAllVersionsDocument(document);
			}
		}
	}
	
	
	@Test
	public void testNoCheckOutMajorDocumentWithVersions() throws Exception {
		Document document = null;
		
		try {
			document = ctrl.createEmptyVersionedDocument(ctrl.getRootFolder(), "checkedOutMajorWithVersions", 
					ResourceController.VERSIONABLE_OBJ_TYPE, VersioningState.MINOR);
			
			if (document.isVersionSeriesCheckedOut()) {
				CmisCancelCheckOut.cancelCheckOutDocument(document, ctrl.getSession());
			}
			
			int odd = 0;
			for (int i = 0; i < 5; i++) {
				document = document.getObjectOfLatestVersion(false);
				CmisCheckOut.checkOutDocument(document);
				
				document = document.getObjectOfLatestVersion(false);
				
				if (odd % 2 == 0) {
					CmisCheckIn.checkInDocument(document, ctrl.getSession(), "major", "some commit");
				} else {
					CmisCheckIn.checkInDocument(document, ctrl.getSession(), "minor", "some commit");
				}
				
				odd++;
			}
			
			String url = CmisURLConnection.generateURLObject(document, ctrl, "/");
			String test = CmisOldVersions.listOldVersions(document, url, "current");
			
			assertTrue(test, test
				.startsWith("{\"v3.0\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252"
						+ "Fatom11%2FA1%2FcheckedOutMajorWithVersions\",\"some commit\",\"admin\"]"));
			
			String[] otherVersion = new String[5];
			
			otherVersion[0] = "\"v2.1\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252"
					+ "Fatom11%2FA1%2FcheckedOutMajorWithVersions?oldversion";
			otherVersion[1] = "\"v2.0\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252"
					+ "Fatom11%2FA1%2FcheckedOutMajorWithVersions?oldversion";
			otherVersion[2] = "\"v1.1\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252"
					+ "Fatom11%2FA1%2FcheckedOutMajorWithVersions?oldversion";
			otherVersion[3] = "\"v1.0\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252"
					+ "Fatom11%2FA1%2FcheckedOutMajorWithVersions?oldversion";
			otherVersion[4] = "\"v0.1\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252"
					+ "Fatom11%2FA1%2FcheckedOutMajorWithVersions?oldversion";
			
			for (int i = 0; i < otherVersion.length; i++) {
				assertTrue(test, test.contains(otherVersion[i]));
			}
				
		} finally {
			if (document != null) {
				ctrl.deleteAllVersionsDocument(document);
			}
		}
	}
	
	
	private static String getFirstVersionID(Document document) {
		List<Document> allVer = document.getAllVersions();
		return allVer.get(allVer.size() - 1).getId();
	}
	
	@Test
	public void testListOldVersions() throws Exception {
	  Document document = null;
		try {
			document = ctrl.createEmptyVersionedDocument(ctrl.getRootFolder(), "check", 
					ResourceController.VERSIONABLE_OBJ_TYPE, VersioningState.MINOR);
			
			for (int i = 0; i < 5; i++) {
				document = document.getObjectOfLatestVersion(false);
				CmisCheckOut.checkOutDocument(document);
				
				document = document.getObjectOfLatestVersion(false);
				CmisCheckIn.checkInDocument(document, cmisAccessProvider.getCmisAccess().getSession(), "major", "");
			}
			
			document = document.getObjectOfLatestVersion(false);
			assertFalse(document.isVersionSeriesCheckedOut());

			String url = CmisURLConnection.generateURLObject(document, ctrl, "/");
			

			String test = CmisOldVersions.listOldVersions(document, url, "current");

			assertTrue(test, test.startsWith(
			    "{\"v5.0\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2Fcheck"));
			
			CmisCheckOut.checkOutDocument(document);
			
			document = document.getObjectOfLatestVersion(false);
			test = CmisOldVersions.listOldVersions(document, url, "current");
			
			System.out.println(test);
			
			assertTrue(test, test.startsWith(
				    "{\"current\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2Fcheck"));
			
			String firstVerID = getFirstVersionID(document);
			
			assertTrue(test, test.endsWith(
				"\"v0.1\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11"
				+ "%2FA1%2Fcheck?oldversion="+ firstVerID +"\",\"\",\"admin\"]}"));
			
		} finally {
		  if (document != null) {
		    ctrl.deleteAllVersionsDocument(document);
		  }
		}
	}
}
