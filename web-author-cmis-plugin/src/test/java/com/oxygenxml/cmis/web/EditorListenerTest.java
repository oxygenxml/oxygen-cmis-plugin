package com.oxygenxml.cmis.web;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.basic.util.URLStreamHandlerFactorySetter;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;

public class EditorListenerTest {

	private URLStreamHandlerFactorySetter setter;
	private URL serverUrl;
	private CMISAccess cmisAccess;
	private ResourceController ctrl;

	@Before
	public void setUp() throws Exception {
		serverUrl = new URL("http://localhost:8080/B/atom11");

		cmisAccess = new CMISAccess();
		cmisAccess.connectToRepo(serverUrl, "A1", new UserCredentials("admin", ""));
		ctrl = cmisAccess.createResourceController();

		new CmisURLConnection(serverUrl, cmisAccess, new UserCredentials("admin", ""));

		setter = new URLStreamHandlerFactorySetter();
		setter.setHandler("cmis", new URLStreamHandler() {

			@Override
			protected URLConnection openConnection(URL u) throws IOException {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}

	@Test
	public void testUtilityMethod() throws Exception {
		Document testDocument = ctrl.createVersionedDocument(ctrl.getRootFolder(), "docs33", "content", "plain/text",
				"cmis:document", VersioningState.NONE);

		assertNotNull(testDocument);
		assertFalse(testDocument.isVersionable());

		try {

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
			listener.utilityMethod(webappPluginWorkspace, documentModel);

			assertNotNull(listener);

		} finally {
			ctrl.deleteAllVersionsDocument(testDocument);
		}
	}

	@After
	public void tearDown() throws Exception {
		setter.tearDown();
	}
}
