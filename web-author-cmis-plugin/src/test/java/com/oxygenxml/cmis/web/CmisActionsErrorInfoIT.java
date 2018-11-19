package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertTrue;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.web.action.CmisActionsUtills;

public class CmisActionsErrorInfoIT {

	@Rule
  	public CmisAccessProvider cmisAccessProvider = new CmisAccessProvider();

	private ResourceController ctrl;
	
	@Before
	public void setUp() throws Exception {
		CMISAccess cmisAccess = cmisAccessProvider.getCmisAccess();
		ctrl = cmisAccess.createResourceController();
	}
	
	@Test
	public void cmisActionsErrorInfoTest() {
		Document testDoc = null;
		Document document = null;
		String resultErrorInfo = null;
		
		try {
			testDoc = ctrl.createEmptyVersionedDocument(ctrl.getRootFolder(), "errorDoc", "errorMimeTyoe", VersioningState.MINOR);
			
			try {
				document = ctrl.createEmptyVersionedDocument(ctrl.getRootFolder(), "errorDoc", "errorMimeTyoe", VersioningState.MINOR);
			} catch (Exception e) {
				resultErrorInfo = CmisActionsUtills.returnErrorInfoJSON("denied", e.getMessage());
			}
			
			assertTrue(resultErrorInfo,
					resultErrorInfo.equals("{\"error\":\"denied\",\"message\""
							+ ":\"409 Conflict for: http://localhost:8080/B/atom11/A1/children?id=100&versioningState=minor\"}"));
			
		} finally {
			if (testDoc != null) {
				ctrl.deleteAllVersionsDocument(testDoc);
			}
			
			if (document != null) {
				ctrl.deleteAllVersionsDocument(document);
			}
		}
	}
	
}
