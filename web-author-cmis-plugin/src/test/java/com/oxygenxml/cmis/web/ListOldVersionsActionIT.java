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
	
	private static final String CURRENT_VERSION_LABEL = "current";
	private final static String MINOR_VERSION_TYPE = "minor";
	private final static String MAJOR_VERSION_TYPE = "major";

	@Before
	public void setUp() throws Exception {
	  CMISAccess cmisAccess = cmisAccessProvider.getCmisAccess();
		ctrl = cmisAccess.createResourceController();
	}
	
	@Test
	public void testNoCheckOutOneVersionOfDocument() throws Exception {
		Document document = null;
		
		try {
			document = createEmptyVersionedDocument("oneVersion");
			
			String url = CmisURLConnection.generateURLObject(document, ctrl, "/");
			String test = CmisOldVersions.listOldVersions(document, url, CURRENT_VERSION_LABEL);
			
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
			document = createEmptyVersionedDocument("checkedOutMajor");
			createNewVersion(document, "major", "some commit");

			String url = CmisURLConnection.generateURLObject(document, ctrl, "/");
			String test = CmisOldVersions.listOldVersions(document, url, CURRENT_VERSION_LABEL);
			
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
			document = createEmptyVersionedDocument("checkedOutMajorWithVersions");
			
			for (int i = 0; i < 5; i++) {
			  createNewVersion(document, (i % 2 == 0) ? MAJOR_VERSION_TYPE : MINOR_VERSION_TYPE, "some commit");
			}
			
			String url = CmisURLConnection.generateURLObject(document, ctrl, "/");
			String test = CmisOldVersions.listOldVersions(document, url, CURRENT_VERSION_LABEL);
			
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
	public void testListOldVersionsLatestIsMajor() throws Exception {
	  Document document = null;
		try {
			document = createEmptyVersionedDocument("check");
			
			for (int i = 0; i < 5; i++) {
			  createNewVersion(document, MAJOR_VERSION_TYPE, "major");
			}
			
			document = document.getObjectOfLatestVersion(false);
			assertFalse(document.isVersionSeriesCheckedOut());

			String url = CmisURLConnection.generateURLObject(document, ctrl, "/");
			

			String test = CmisOldVersions.listOldVersions(document, url, CURRENT_VERSION_LABEL);

			assertTrue(test, test.startsWith(
			    "{\"v5.0\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2Fcheck"));
			
			CmisCheckOut.checkOutDocument(document);
			
			document = document.getObjectOfLatestVersion(false);
			test = CmisOldVersions.listOldVersions(document, url, CURRENT_VERSION_LABEL);
			
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
	
	/**
   * Test that checking out a minor version will create a "current" version.
   * @throws Exception
   */
  @Test
  public void testLatestVersionCheckOutOnMinorVersion() throws Exception {
    Document document = null;
    
    try {
      document = createEmptyVersionedDocument("checkedOutMajor");
      createNewVersion(document, "minor", "some commit");

      String url = CmisURLConnection.generateURLObject(document, ctrl, "/");
      String test = CmisOldVersions.listOldVersions(document, url, CURRENT_VERSION_LABEL);
      
      assertTrue(test, test
        .startsWith("{\"v0.2\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252"
            + "Fatom11%2FA1%2FcheckedOutMajor\",\"some commit\",\"admin\"]"));
      
      CmisCheckOut.checkOutDocument(document);
      document = document.getObjectOfLatestVersion(false);
      test = CmisOldVersions.listOldVersions(document, url, CURRENT_VERSION_LABEL);
      assertTrue(test, test.startsWith(
          "{\"current\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2Fcheck"));
    } finally {
      if (document != null) {
        ctrl.deleteAllVersionsDocument(document);
      }
    }
  }
  
  
  /**
   * Test that checking out a major version will create a "current" version.
   * @throws Exception
   */
  @Test
  public void testLatestVersionCheckOutOnMajorVersion() throws Exception {
    Document document = null;
    
    try {
      document = createEmptyVersionedDocument("checkedOutMajor");
      createNewVersion(document, "major", "some commit");

      String url = CmisURLConnection.generateURLObject(document, ctrl, "/");
      String test = CmisOldVersions.listOldVersions(document, url, CURRENT_VERSION_LABEL);
      
      assertTrue(test, test
        .startsWith("{\"v1.0\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252"
            + "Fatom11%2FA1%2FcheckedOutMajor\",\"some commit\",\"admin\"]"));
      
      CmisCheckOut.checkOutDocument(document);
      document = document.getObjectOfLatestVersion(false);
      test = CmisOldVersions.listOldVersions(document, url, CURRENT_VERSION_LABEL);
      assertTrue(test, test.startsWith(
          "{\"current\":[\"?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2Fcheck"));
    } finally {
      if (document != null) {
        ctrl.deleteAllVersionsDocument(document);
      }
    }
  }
	
	/**
	 * Create a new empty versioned file.
	 * @param fileName
	 * @return
	 */
	private Document createEmptyVersionedDocument(String fileName) {
    Document document = ctrl.createEmptyVersionedDocument(
      ctrl.getRootFolder(), 
      fileName, 
      ResourceController.VERSIONABLE_OBJ_TYPE, 
      VersioningState.MINOR
    );
    // Should not be checked-out right after creation.
    assertFalse(document.isVersionSeriesCheckedOut());
    return document;
  }
	
	/**
	 * Creates a new version for the document.
	 * @param document
	 * @param versionType
	 * @param commitMessage
	 * @throws Exception
	 */
	private void createNewVersion(Document document, String versionType, String commitMessage) throws Exception {
    document = document.getObjectOfLatestVersion(false);
    CmisCheckOut.checkOutDocument(document);
    
    document = document.getObjectOfLatestVersion(false); // is this needed?
    CmisCheckIn.checkInDocument(document, ctrl.getSession(), versionType, commitMessage);
  }
}
