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
			
			String test = CmisOldVersions.listOldVersions(document, url);

			assertTrue(test, test.startsWith(
			    "{\"v5.0\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2Fcheck"));
			
			CmisCheckOut.checkOutDocument(document);
			
			document = document.getObjectOfLatestVersion(false);
			test = CmisOldVersions.listOldVersions(document, url);
			
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
