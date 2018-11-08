package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertTrue;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.web.action.CmisCheckOut;

public class CmisActionsCheckOutIT {

  @Rule
  public CmisAccessProvider cmisAccessProvider = new CmisAccessProvider();
  private ResourceController ctrl;

	@Before
	public void setUp() throws Exception {
    CMISAccess cmisAccess = cmisAccessProvider.getCmisAccess();
    ctrl = cmisAccess.createResourceController();
	}

	@Test
	public void testCheckOut() throws Exception {
	  Document document = null;
		try {
      document = ctrl.createEmptyVersionedDocument(
          ctrl.getRootFolder(), "checkout", "plain/xml", VersioningState.MINOR);
			CmisCheckOut.checkOutDocument(document);

			assertTrue(document.isVersionable());

			document = document.getObjectOfLatestVersion(false);
			assertTrue(document.isVersionSeriesCheckedOut());
			
		} finally {
		  if (document != null) {
		    ctrl.deleteAllVersionsDocument(document);
		  }
		}
	}
}
