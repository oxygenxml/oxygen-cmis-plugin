package com.oxygenxml.cmis.web;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

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
import ro.sync.exml.workspace.api.options.WSOptionsStorage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class EditorListener implements WorkspaceAccessPluginExtension {

	private static final Logger logger = Logger.getLogger(EditorListener.class.getName());

	@Override
	public void applicationStarted(StandalonePluginWorkspace pluginWorkspaceAccess) {

		WebappPluginWorkspace webappPluginWorkspace = (WebappPluginWorkspace) pluginWorkspaceAccess;
		webappPluginWorkspace.addEditingSessionLifecycleListener(new WebappEditingSessionLifecycleListener() {

			@Override
			public void editingSessionStarted(String sessionId, AuthorDocumentModel documentModel) {
				utilityMethod(webappPluginWorkspace, documentModel);
			}
		});
	}

	private void utilityMethod(WebappPluginWorkspace webappPluginWorkspace, AuthorDocumentModel documentModel) {
		AuthorAccess authorAccess = documentModel.getAuthorAccess();
		authorAccess.getWorkspaceAccess();

		SessionStore sessionStore = webappPluginWorkspace.getSessionStore();

		// Get URL and ContextID for CmisURLConnection
		URL url = authorAccess.getEditorAccess().getEditorLocation();
		String contextId = url.getUserInfo();
		String urlWithoutContextId = URLStreamHandlerWithContextUtil.getInstance().toStrippedExternalForm(url);
		UserCredentials credentials = sessionStore.get(contextId, "credentials");
		CmisURLConnection connection = new CmisURLConnection(url, new CMISAccess(), credentials);

		// TODO: make editor read-only.!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		try {
			logger.info("EditorListener is loaded!");

			Document document = (Document) connection.getCMISObject(urlWithoutContextId);
			if (!document.isVersionable()) {
				
				documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
						.setPseudoClass("nonversionable");
				
			}
			if (document.isVersionSeriesCheckedOut()) {
				
				String versionSeriesCheckedOutBy = document.getVersionSeriesCheckedOutBy();
				
				if (!credentials.getUsername().equals(versionSeriesCheckedOutBy)) {

					documentModel.getAuthorAccess().getEditorAccess()
							.setReadOnly(new ReadOnlyReason("Document is checked out by " + versionSeriesCheckedOutBy));
					documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
							.setPseudoClass("anotheruser");
					
				} else {
					
					documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
							.setPseudoClass("checkedout");
					
				}
			} else {
				if (isCheckOutRequired() && !document.isVersionSeriesCheckedOut()) {
					
					documentModel.getAuthorAccess().getEditorAccess()
							.setReadOnly(new ReadOnlyReason("Check-out required!"));
					
				}
			}
		} catch (CmisUnauthorizedException e1) {
			e1.printStackTrace();
		} catch (CmisObjectNotFoundException e1) {
			e1.printStackTrace();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
	}

	public static boolean isCheckOutRequired() {
		WSOptionsStorage optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
		String optionValue = optionsStorage.getOption(CmisPluginConfigExtension.CHECKOUT_REQUIRED, "on");
		return "on".equals(optionValue);
	}

	@Override
	public boolean applicationClosing() {
		return true;
	}

}
