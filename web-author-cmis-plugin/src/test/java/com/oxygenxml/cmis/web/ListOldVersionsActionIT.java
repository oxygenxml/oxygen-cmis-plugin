package com.oxygenxml.cmis.web;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.action.CmisCheckIn;
import com.oxygenxml.cmis.web.action.CmisCheckOut;
import com.oxygenxml.cmis.web.action.CmisOldVersions;

import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class ListOldVersionsActionIT {

  @Rule
  public CmisAccessProvider cmisAccessProvider = new CmisAccessProvider();

	private ResourceController ctrl;
	
	private final static String MINOR_VERSION_TYPE = "minor";
	private final static String MAJOR_VERSION_TYPE = "major";

	@Before
	public void setUp() throws Exception {
	  CMISAccess cmisAccess = cmisAccessProvider.getCmisAccess();
		ctrl = cmisAccess.createResourceController();
		WebappPluginWorkspace webappPluginWorkspace = Mockito.mock(WebappPluginWorkspace.class);
		PluginWorkspaceProvider.setPluginWorkspace(webappPluginWorkspace);

		PluginResourceBundle rb = Mockito.mock(PluginResourceBundle.class);
		when(webappPluginWorkspace.getResourceBundle()).thenReturn(rb);
		doAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArguments()[0].toString();
			}
		}).when(rb).getMessage(anyString());
	}

	@After
	public void tearDown() throws Exception {
		PluginWorkspaceProvider.setPluginWorkspace(null);
	}
	
	@Test
	public void testNoCheckOutOneVersionOfDocument() throws Exception {
		Document document = null;
		
		try {
			document = createEmptyVersionedDocument("oneVersion");
			List<Document> allVersions = document.getObjectOfLatestVersion(false).getAllVersions();
			String url = CmisURLConnection.generateURLObject(ctrl.getRootFolder(), document, ctrl);
			
      List<Map<String, String>> versions = getVersions(document, url);
      
      Map<String, String> latestVersion = versions.get(0);
      assertEquals("0.1", latestVersion.get("version"));
      assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/oneVersion?oldversion=" + allVersions.get(0).getId(),
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
			List<Document> allVersions = document.getObjectOfLatestVersion(false).getAllVersions();
			String url = CmisURLConnection.generateURLObject(ctrl.getRootFolder(), document, ctrl);
			List<Map<String, String>> versions = getVersions(document, url);
			
      Map<String, String> latestVersion = versions.get(0);
      assertEquals("1.0", latestVersion.get("version"));
      assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/checkedOutMajor?oldversion=" + allVersions.get(0).getId(),
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
			
			List<Document> allVersions = document.getObjectOfLatestVersion(false).getAllVersions();
			String url = CmisURLConnection.generateURLObject(ctrl.getRootFolder(), document, ctrl);
			List<Map<String, String>> versions = getVersions(document, url);
			
			Map<String, String> latestVersion = versions.get(0);
			assertEquals("3.0", latestVersion.get("version"));
		  assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/checkedOutMajorWithVersions?oldversion=" + allVersions.get(0).getId(),
		      latestVersion.get("url"));
      assertEquals("some commit", latestVersion.get("commitMessage"));
      assertEquals("admin", latestVersion.get("author"));
			
      assertEquals("2.1", versions.get(1).get("version"));
      assertEquals("2.0", versions.get(2).get("version"));
      assertEquals("1.1", versions.get(3).get("version"));
      assertEquals("1.0", versions.get(4).get("version"));
      assertEquals("0.1", versions.get(5).get("version"));
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
			List<Document> allVersions = document.getObjectOfLatestVersion(false).getAllVersions();

			assertFalse(document.isVersionSeriesCheckedOut());
			String url = CmisURLConnection.generateURLObject(ctrl.getRootFolder(), document, ctrl);
			
      List<Map<String, String>> versions = getVersions(document, url);
			
			Map<String, String> latestVersion = versions.get(0);
			assertEquals("5.0", latestVersion.get("version"));
			assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/check?oldversion=" + allVersions.get(0).getId(),
			    latestVersion.get("url"));
			
			// checkout document to create current version.
			CmisCheckOut.checkOutDocument(document);
			versions = getVersions(document, url);
			
			document = document.getObjectOfLatestVersion(false);
      latestVersion = versions.get(0);
      assertEquals("Current", latestVersion.get("version"));
      assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/check", latestVersion.get("url"));
			
			String firstVerID = getFirstVersionID(document);

			Map<String, String> firstVersion = versions.get(versions.size() - 1);
      assertEquals("0.1", firstVersion.get("version"));
      assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/check?oldversion="+ firstVerID, 
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
  private List<Map<String, String>> getVersions(Document document, String url)
      throws IOException, JsonParseException, JsonMappingException {
    UserCredentials testUser = new UserCredentials("admin", null);
    return CmisOldVersions.listOldVersions(document, new URL(url), testUser);
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
      List<Document> allVersions = document.getObjectOfLatestVersion(false).getAllVersions();

      String url = CmisURLConnection.generateURLObject(ctrl.getRootFolder(), document, ctrl);
      List<Map<String, String>> versions = getVersions(document, url);
      
      Map<String, String> latestVersion = versions.get(0);
      assertEquals("0.2", latestVersion.get("version"));
      assertEquals("some commit", latestVersion.get("commitMessage"));
      assertEquals("admin", latestVersion.get("author"));
      assertEquals("false", latestVersion.get("isCurrentVersion"));
      assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/checkedOutMajor?oldversion=" + allVersions.get(0).getId(),
          latestVersion.get("url"));
      
      CmisCheckOut.checkOutDocument(document);
      document = document.getObjectOfLatestVersion(false);
      
      versions = getVersions(document, url);
      latestVersion = versions.get(0);
      
      assertEquals("Current", latestVersion.get("version"));
      assertEquals("admin", latestVersion.get("author"));
      assertEquals("true", latestVersion.get("isCurrentVersion"));
      assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/checkedOutMajor",
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
      List<Document> allVersions = document.getObjectOfLatestVersion(false).getAllVersions();

      String url = CmisURLConnection.generateURLObject(ctrl.getRootFolder(), document, ctrl);
      
      List<Map<String, String>> versions = getVersions(document, url);
      
      Map<String, String> latestVersion = versions.get(0);
      assertEquals("1.0", latestVersion.get("version"));
      assertEquals("some commit", latestVersion.get("commitMessage"));
      assertEquals("admin", latestVersion.get("author"));
      assertEquals("false", latestVersion.get("isCurrentVersion"));
      assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/checkedOutMajor?oldversion=" + allVersions.get(0).getId(),
          latestVersion.get("url"));
      
      CmisCheckOut.checkOutDocument(document);
      document = document.getObjectOfLatestVersion(false);
      
      versions = getVersions(document, url);
      latestVersion = versions.get(0);
      
      assertEquals("Current", latestVersion.get("version"));
      assertEquals("admin", latestVersion.get("author"));
      assertEquals("true", latestVersion.get("isCurrentVersion"));
      assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/checkedOutMajor",
          latestVersion.get("url"));
    } finally {
      if (document != null) {
        ctrl.deleteAllVersionsDocument(document);
      }
    }
  }
	
  /**
   * Test that the author or each revision is correct.
   * @throws Exception
   */
  @Test
  public void testAuthorIsCorrectlyDisplayed() throws Exception {
    Document document = null;
    try {
      document = createEmptyVersionedDocument("multi-user-history.xml");
      
      createNewMajorVersionAsUser(document, "admin");
      assertEquals("admin", getLastModifiedBy(document));
      
      createNewMajorVersionAsUser(document, "other-user");
      assertEquals("other-user", getLastModifiedBy(document));
      
      createNewMajorVersionAsUser(document, "admin");
      assertEquals("admin", getLastModifiedBy(document));
      
      String url = CmisURLConnection.generateURLObject(ctrl.getRootFolder(), document, ctrl);
      
      List<Map<String, String>> versions = getVersions(document, url);
      
      List<String> authors = versions.stream().map(version -> version.get("author")).collect(toList());
      assertEquals(Arrays.asList("admin", "other-user", "admin", "admin"), authors);
    } finally {
      if (document != null) {
        ctrl.deleteAllVersionsDocument(document);
      }
    }
  }

  private String getLastModifiedBy(Document document) {
    return document.getObjectOfLatestVersion(true).getLastModifiedBy();
  }

  /**
   * Create a new version of the document, as another user.
   * @param document The document.
   * @param otherUserName The other user name.
   * @throws Exception If it fails.
   */
  private void createNewMajorVersionAsUser(Document document, String otherUserName) throws Exception {
    CMISAccess accessForOtherUser = cmisAccessProvider.createCmisAccessForUserName(otherUserName);
    ResourceController controllerForOtherUser = accessForOtherUser.createResourceController();
    Document documentForOtherUser = controllerForOtherUser.getDocument(document.getId());
    createNewVersion(documentForOtherUser, MAJOR_VERSION_TYPE, "other commit message");
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
    CmisCheckIn.checkInDocument(document, versionType, commitMessage);
  }
}
