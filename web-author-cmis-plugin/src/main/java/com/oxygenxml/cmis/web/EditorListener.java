package com.oxygenxml.cmis.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.action.CmisActions;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.access.WebappEditingSessionLifecycleListener;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.ReadOnlyReason;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class EditorListener implements WorkspaceAccessPluginExtension {

	private static final Logger logger = Logger.getLogger(EditorListener.class.getName());

	private static final String NON_VERSIONABLE 	  = "nonversionable";
	private static final String IS_CHECKED_OUT        = "checkedout";
	private static final String TO_BLOCK              = "block";
	private static final String NO_SUPPORT			  = "nosupportfor";

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

	/**
	 * Receive some information about document to client-side code.
	 * 
	 * @param webappPluginWorkspace
	 * @param documentModel
	 */
	@VisibleForTesting
	public void utilityMethod(WebappPluginWorkspace webappPluginWorkspace, AuthorDocumentModel documentModel) {
		AuthorAccess authorAccess = documentModel.getAuthorAccess();
		authorAccess.getWorkspaceAccess();

		SessionStore sessionStore = webappPluginWorkspace.getSessionStore();

		// Get URL and ContextID for CmisURLConnection
		URL url = authorAccess.getEditorAccess().getEditorLocation();
		String urlWithoutContextId = null;
		
		if(url != null) {
			urlWithoutContextId = url.getProtocol() + "://" + url.getHost() + url.getPath();
		}
		
		String contextId = url.getUserInfo();
		UserCredentials credentials = sessionStore.get(contextId, "wa-cmis-plugin-credentials");
		CmisURLConnection connection = new CmisURLConnection(url, new CMISAccess(), credentials);

		PluginResourceBundle rb = webappPluginWorkspace.getResourceBundle();

		try {
			logger.info("EditorListener is loaded!");

			if (url.getQuery() != null && url.getQuery().contains(CmisActions.OLD_VERSION)) {
				HashMap<String, String> queryPart = new HashMap<>();
				
				for(String pair : url.getQuery().split("&")) {
					int index = pair.indexOf("=");
					queryPart.put(pair.substring(0, index), pair.substring(index + 1));
				}
				
				String objectId = queryPart.get(CmisActions.OLD_VERSION);
				Document oldDoc = (Document) connection.getResourceController(urlWithoutContextId).getCmisObj(objectId);

				String language = webappPluginWorkspace.getUserInterfaceLanguage();
				SimpleDateFormat df = new SimpleDateFormat("d MMM yyyy HH:mm:ss", new Locale(language));
				
				Date lastMod = oldDoc.getLastModificationDate().getTime();
				
				documentModel.getAuthorAccess().getEditorAccess().setReadOnly(
						new ReadOnlyReason(rb.getMessage(TranslationTags.OLD_VER_WARNING) + " : " + df.format(lastMod)));
				documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
						.setPseudoClass(TO_BLOCK);
				
				if(oldDoc.isPrivateWorkingCopy() == null || oldDoc.getCheckinComment() == null) {
					documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
					.setPseudoClass(NO_SUPPORT);
				}

			} else {
				Document document = (Document) connection.getCMISObject(urlWithoutContextId);

				if(document.isPrivateWorkingCopy() == null || document.getCheckinComment() == null) {
					documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
					.setPseudoClass(NO_SUPPORT);
				}
				
				if (!document.isVersionable()) {
					documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
							.setPseudoClass(NON_VERSIONABLE);
				} else if (document.isVersionSeriesCheckedOut()) {
					String versionSeriesCheckedOutBy = document.getVersionSeriesCheckedOutBy();

					if (!credentials.getUsername().equals(versionSeriesCheckedOutBy)) {
						documentModel.getAuthorAccess().getEditorAccess().setReadOnly(new ReadOnlyReason(
								rb.getMessage(TranslationTags.CHECKED_OUT_BY) + " " + versionSeriesCheckedOutBy));
						documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
								.setPseudoClass(TO_BLOCK);
					} else {
						documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
								.setPseudoClass(IS_CHECKED_OUT);
					}

				} else {
					if (isCheckOutRequired() && !document.isVersionSeriesCheckedOut()) {
						documentModel.getAuthorAccess().getEditorAccess()
								.setReadOnly(new ReadOnlyReason(rb.getMessage(TranslationTags.CHECK_OUT_REQ_EDITOR)));
					}
				}
			}

		} catch (CmisUnauthorizedException e1) {
			logger.info(e1.getMessage());
		} catch (CmisObjectNotFoundException e1) {
			logger.info(e1.getMessage());
		} catch (MalformedURLException e1) {
			logger.info(e1.getMessage());
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
