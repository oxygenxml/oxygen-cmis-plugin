package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
	
	
	@Test
	public void testListOldVersions() throws Exception {
	  Document document = null;
		try {
      document = ctrl.createEmptyVersionedDocument(ctrl.getRootFolder(), "check", 
          ResourceController.VERSIONABLE_OBJ_TYPE, VersioningState.MINOR);
		  
			CmisCheckOut.checkOutDocument(document);

			assertNotNull(document);
			assertTrue(document.isVersionable());

			document = document.getObjectOfLatestVersion(false);
			CmisCheckIn.checkInDocument(document, cmisAccessProvider.getCmisAccess().getSession(), "major", "");

			document = document.getObjectOfLatestVersion(false);
			assertFalse(document.isVersionSeriesCheckedOut());

			String url = CmisURLConnection.generateURLObject(document, ctrl, "/");

			assertNotNull(url);
			assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/check", url);

			String test = CmisOldVersions.listOldVersions(document, url);

			System.out.println(test);
			assertNotNull(test);
			assertTrue(test.startsWith("{\"v1.0\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost"
					+ "%253A8080%252FB%252Fatom11%2FA1%2Fcheck?oldversion"));

			assertTrue(test.contains("admin"));
			
		} finally {
		  if (document != null) {
		    ctrl.deleteAllVersionsDocument(document);
		  }
		}
	}
}
