package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
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
			
      ArrayList<HashMap<String, String>> versions = getVersions(document, url);
      
      HashMap<String, String> latestVersion = versions.get(0);
      assertEquals("v0.1", latestVersion.get("version"));
      assertEquals("?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2FoneVersion",
          latestVersion.get("url"));
      assertEquals("admin", latestVersion.get("author"));
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
			ArrayList<HashMap<String, String>> versions = getVersions(document, url);
			
      HashMap<String, String> latestVersion = versions.get(0);
      assertEquals("v1.0", latestVersion.get("version"));
      assertEquals("?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2FcheckedOutMajor",
          latestVersion.get("url"));
      assertEquals("some commit", latestVersion.get("commitMessage"));
      assertEquals("admin", latestVersion.get("author"));
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
			ArrayList<HashMap<String, String>> versions = getVersions(document, url);
			
			HashMap<String, String> latestVersion = versions.get(0);
			assertEquals("v3.0", latestVersion.get("version"));
		  assertEquals("?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2FcheckedOutMajorWithVersions",
		      latestVersion.get("url"));
      assertEquals("some commit", latestVersion.get("commitMessage"));
      assertEquals("admin", latestVersion.get("author"));
			
      assertEquals("v2.1", versions.get(1).get("version"));
      assertEquals("v2.0", versions.get(2).get("version"));
      assertEquals("v1.1", versions.get(3).get("version"));
      assertEquals("v1.0", versions.get(4).get("version"));
      assertEquals("v0.1", versions.get(5).get("version"));
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
			
      ArrayList<HashMap<String, String>> versions = getVersions(document, url);
			
			HashMap<String, String> latestVersion = versions.get(0);
			assertEquals("v5.0", latestVersion.get("version"));
			assertEquals("?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2Fcheck", latestVersion.get("url"));
			
			// checkout document to create current version.
			CmisCheckOut.checkOutDocument(document);
			versions = getVersions(document, url);
			
			document = document.getObjectOfLatestVersion(false);
      latestVersion = versions.get(0);
      assertEquals("current", latestVersion.get("version"));
      assertEquals("?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2Fcheck", latestVersion.get("url"));
			
			String firstVerID = getFirstVersionID(document);

			HashMap<String, String> firstVersion = versions.get(versions.size() - 1);
      assertEquals("v0.1", firstVersion.get("version"));
      assertEquals("?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2Fcheck?oldversion="+ firstVerID, 
          firstVersion.get("url"));
		} finally {
		  if (document != null) {
		    ctrl.deleteAllVersionsDocument(document);
		  }
		}
	}

	/**
	 * Retrieves the versions of the document as an map array.
	 * 
	 * @param document the document.
	 * @param url the document's url.
	 * 
	 * @return the versions of the document as an map array.
	 * 
	 * @throws IOException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 */
  private ArrayList<HashMap<String, String>> getVersions(Document document, String url)
      throws IOException, JsonParseException, JsonMappingException {
    String test = CmisOldVersions.listOldVersions(document, url, CURRENT_VERSION_LABEL);
    ArrayList<HashMap<String, String>> versions = new ObjectMapper().readValue(test, new TypeReference<ArrayList<HashMap<String, String>>>() {});
    return versions;
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
      ArrayList<HashMap<String, String>> versions = getVersions(document, url);
      
      HashMap<String, String> latestVersion = versions.get(0);
      assertEquals("v0.2", latestVersion.get("version"));
      assertEquals("some commit", latestVersion.get("commitMessage"));
      assertEquals("admin", latestVersion.get("author"));
      assertEquals("?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2FcheckedOutMajor",
          latestVersion.get("url"));
      
      CmisCheckOut.checkOutDocument(document);
      document = document.getObjectOfLatestVersion(false);
      
      versions = getVersions(document, url);
      latestVersion = versions.get(0);
      
      assertEquals("current", latestVersion.get("version"));
      assertEquals("admin", latestVersion.get("author"));
      assertEquals("?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2Fcheck",
          latestVersion.get("url"));
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
      
      ArrayList<HashMap<String, String>> versions = getVersions(document, url);
      
      HashMap<String, String> latestVersion = versions.get(0);
      assertEquals("v1.0", latestVersion.get("version"));
      assertEquals("some commit", latestVersion.get("commitMessage"));
      assertEquals("admin", latestVersion.get("author"));
      assertEquals("?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2FcheckedOutMajor",
          latestVersion.get("url"));
      
      CmisCheckOut.checkOutDocument(document);
      document = document.getObjectOfLatestVersion(false);
      
      versions = getVersions(document, url);
      latestVersion = versions.get(0);
      
      assertEquals("current", latestVersion.get("version"));
      assertEquals("admin", latestVersion.get("author"));
      assertEquals("?url=cmis%3A%2F%2Fhttp%253A%252F%252Flocalhost%253A8080%252FB%252Fatom11%2FA1%2Fcheck",
          latestVersion.get("url"));
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
