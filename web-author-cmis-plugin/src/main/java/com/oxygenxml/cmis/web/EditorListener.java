package com.oxygenxml.cmis.web;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.access.WebappEditingSessionLifecycleListener;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContextUtil;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.ReadOnlyReason;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class EditorListener implements WorkspaceAccessPluginExtension {

	@Override
	public void applicationStarted(StandalonePluginWorkspace pluginWorkspaceAccess) {

		((WebappPluginWorkspace) pluginWorkspaceAccess)
				.addEditingSessionLifecycleListener(new WebappEditingSessionLifecycleListener() {

					@Override
					public void editingSessionStarted(String sessionId, AuthorDocumentModel documentModel) {

						AuthorAccess authorAccess = documentModel.getAuthorAccess();
						authorAccess.getWorkspaceAccess();

						// Get Session Store
						WebappPluginWorkspace workspace = (WebappPluginWorkspace) PluginWorkspaceProvider
								.getPluginWorkspace();
						SessionStore sessionStore = workspace.getSessionStore();

						// Get URL and ContextID
						URL url = authorAccess.getEditorAccess().getEditorLocation();
						String contextId = url.getUserInfo();
						String urlWithoutContextId = URLStreamHandlerWithContextUtil.getInstance()
								.toStrippedExternalForm(url);

						// Use Session Store to get User Credentials from ContextID
						UserCredentials credentials = sessionStore.get(contextId, "credentials");

						// Create new connection instance for CmisActionBase
						CmisURLConnection connection = new CmisURLConnection(url, new CMISAccess(), credentials);

						// TODO: make editor read-only.
						try {
							Document document = (Document) connection.getCMISObject(urlWithoutContextId);

							if (document.isVersionSeriesCheckedOut()) {
								documentModel.getAuthorAccess().getEditorAccess().setReadOnly(
										new ReadOnlyReason("Document is checked out by " + document.getLastModifiedBy()
												+ " " + document.getLastModificationDate().getTime().toString()));
							}
						} catch (CmisUnauthorizedException e1) {
							e1.printStackTrace();
						} catch (CmisObjectNotFoundException e1) {
							e1.printStackTrace();
						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						}
					}
				});

	}

	@Override
	public boolean applicationClosing() {
		return true;
	}

}
