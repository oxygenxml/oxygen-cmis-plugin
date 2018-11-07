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
import com.oxygenxml.cmis.web.action.CmisCheckIn;
import com.oxygenxml.cmis.web.action.CmisCheckOut;

public class CmisActionsCheckInIT {
  @Rule
  public CmisAccessProvider cmisAccessProvider = new CmisAccessProvider();
	private ResourceController ctrl;

	@Before
	public void setUp() throws Exception {
		CMISAccess cmisAccess = cmisAccessProvider.getCmisAccess();
		ctrl = cmisAccess.createResourceController();
	}

	@Test
	public void testCheckIn() throws Exception {
		Document document = null;
    try {
    		document = ctrl.createVersionedDocument(ctrl.getRootFolder(), "checkin", "empty", "plain/xml",
  				"VersionableType", VersioningState.MINOR);
  		
    		String commitMessage = "important commit!";

			CmisCheckOut.checkOutDocument(document);

			assertNotNull(document);
			assertTrue(document.isVersionable());

			document = document.getObjectOfLatestVersion(false);
			assertTrue(document.isVersionSeriesCheckedOut());

			CmisCheckIn.checkInDocument(document, cmisAccessProvider.getCmisAccess().getSession(), "minor", commitMessage);

			document = document.getObjectOfLatestVersion(false);
			assertFalse(document.isVersionSeriesCheckedOut());
			
			assertEquals(commitMessage, document.getCheckinComment());
			
		} finally {
		  if (document != null) {
		    ctrl.deleteAllVersionsDocument(document);
		  }
		}
	}
}
