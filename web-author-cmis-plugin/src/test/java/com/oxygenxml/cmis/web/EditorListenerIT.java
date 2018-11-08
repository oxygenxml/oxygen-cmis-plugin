package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;

public class EditorListenerIT {
  @Rule
  public CmisAccessProvider cmisAccessProvider = new CmisAccessProvider();
	private ResourceController ctrl;

	@Before
	public void setUp() throws Exception {
	  CMISAccess cmisAccess = cmisAccessProvider.getCmisAccess();
		ctrl = cmisAccess.createResourceController();
	}

	@Test
	public void testSessionStarted() throws Exception {
	  Document testDocument = null;
		try {
      String filename = "docs33.xml";
      ContentStream contentStream = ctrl.createXmlUtf8ContentStream(filename, "<root/>");
      testDocument = ctrl.createVersionedDocument(ctrl.getRootFolder(), filename, 
          contentStream,
          ResourceController.DEFAULT_OBJ_TYPE,
	        VersioningState.NONE);

	    assertNotNull(testDocument);
	    assertFalse(testDocument.isVersionable());
			URL url = new URL(CmisURLConnection.generateURLObject(testDocument, ctrl, "/"));
			String contextId = "some important info credentials";

			assertNotNull(url);
			assertNotNull(contextId);

			WebappPluginWorkspace webappPluginWorkspace = Mockito.mock(WebappPluginWorkspace.class);

			AuthorEditorAccess editorAccess = Mockito.mock(AuthorEditorAccess.class);
			Mockito.when(editorAccess.getEditorLocation()).thenReturn(url);

			AuthorAccess authorAccess = Mockito.mock(AuthorAccess.class);
			Mockito.when(authorAccess.getEditorAccess()).thenReturn(editorAccess);

			AuthorDocumentModel documentModel = Mockito.mock(AuthorDocumentModel.class);
			Mockito.when(documentModel.getAuthorAccess()).thenReturn(authorAccess);

			SessionStore sessionStore = Mockito.mock(SessionStore.class);
			UserCredentials credentials = new UserCredentials("admin", "");

			assertNotNull(credentials);
			assertEquals(credentials.getUsername(), "admin");

			Mockito.when(webappPluginWorkspace.getSessionStore()).thenReturn(sessionStore);
			Mockito.when(sessionStore.get(contextId, "credentials")).thenReturn(credentials);

			AuthorDocumentController authorDocCtrl = Mockito.mock(AuthorDocumentController.class);
			Mockito.when(documentModel.getAuthorDocumentController()).thenReturn(authorDocCtrl);

			AuthorDocument node = Mockito.mock(AuthorDocument.class);
			Mockito.when(authorDocCtrl.getAuthorDocumentNode()).thenReturn(node);

			AuthorElement element = Mockito.mock(AuthorElement.class);
			Mockito.when(node.getRootElement()).thenReturn(element);

			EditorListener listener = new EditorListener();
			listener.editingSessionStarted(webappPluginWorkspace, documentModel);

			assertNotNull(listener);

		} finally {
		  if (testDocument != null) {
		    ctrl.deleteAllVersionsDocument(testDocument);
		  }
		}
	}
}
