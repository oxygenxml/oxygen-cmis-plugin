package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.EditorListener;
import com.oxygenxml.cmis.web.TranslationTags;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorOperationWithResult;
import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContextUtil;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.ReadOnlyReason;

@WebappRestSafe
public class CmisActions extends AuthorOperationWithResult {
	private static final Logger logger = Logger.getLogger(CmisActions.class.getName());

	private CmisURLConnection connection;
	private UserCredentials credentials;
	private Document document;
	
	private static String oldVersionJson = null;
	private static String errorInformation = null;

	public static final String OLD_VERSION = "oldversion";
	
	private static final String CHECK_OUT 	      = "cmisCheckout";
	private static final String CHECK_IN 		  = "cmisCheckin";
	private static final String CANCEL_CHECK_OUT  = "cancelCmisCheckout";
	private static final String LIST_VERSIONS     = "listOldVersions";
	private static final String COMMIT_MESSAGE    = "commit";
	private static final String ACTION            = "action";
	private static final String STATE             = "state";

	@Override
	public String getDescription() {
		return "";
	}

	private String errorInfoBuilder(String errorType, String errorInfo) {
		StringBuilder infoBuilder = new StringBuilder();

		infoBuilder.append("{");
		infoBuilder.append("\"error\"").append(":");
		infoBuilder.append("\"" + errorType + "\"");
		
		if(errorInfo != null) {
			infoBuilder.append(",");
			infoBuilder.append("\"message\"").append(":");
			infoBuilder.append("\"" + errorInfo + "\"");
		}
		
		infoBuilder.append("}");
		//new ObjectMapper().writevs
		return infoBuilder.toString();
	}

	/**
	 * Check which action was received and do operation.
	 * 
	 * @param actualAction
	 * @param actualState
	 * @param commitMessage
	 * @param authorAccess
	 * @param url
	 */
	private void actionManipulator(String actualAction, String actualState, String commitMessage, String url,
			AuthorAccess authorAccess, AuthorDocumentModel model) {
		
		PluginResourceBundle rb = ((WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace())
				.getResourceBundle();

		oldVersionJson = null;
		Session session = connection.getCMISAccess().getSession();
		
		try {
			if (actualAction.equals(CHECK_OUT)) {
				CmisCheckOutAction.checkOutDocument(document);
				if (EditorListener.isCheckOutRequired()) {
					authorAccess.getEditorAccess().setEditable(true);
				}

				errorInformation = errorInfoBuilder("you_shall_not_pass", null);
			}
			if (actualAction.equals(CANCEL_CHECK_OUT)) {
				
        CmisCheckOutAction.cancelCheckOutDocument(document, session);
				if (EditorListener.isCheckOutRequired()) {
					authorAccess.getEditorAccess()
							.setReadOnly(new ReadOnlyReason(rb.getMessage(TranslationTags.CHECK_OUT_REQUIRED)));
				}
			}
			if (actualAction.equals(CHECK_IN)) {
				CmisCheckInAction.checkInDocument(document, session, actualState, commitMessage);
				if (EditorListener.isCheckOutRequired()) {
					authorAccess.getEditorAccess()
							.setReadOnly(new ReadOnlyReason(rb.getMessage(TranslationTags.CHECK_OUT_REQUIRED)));
				}
			}
			if (actualAction.equals(LIST_VERSIONS)) {
				oldVersionJson = ListOldVersionsAction.listOldVersions(document, url);
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
			
			errorInformation = errorInfoBuilder("denied", e.getMessage());
		}
	}

	/**
	 * Get the action from ArgumentsMap, get connection with server and call
	 * actionManipulator.
	 */
	@Override
	public String doOperation(AuthorDocumentModel model, ArgumentsMap args)
			throws IllegalArgumentException, AuthorOperationException {

		AuthorAccess authorAccess = model.getAuthorAccess();
		authorAccess.getWorkspaceAccess();

		// Get Session Store
		String urlWithoutContextId = getUrlWithoutContextId(authorAccess);

		try {
			document = (Document) connection.getCMISObject(urlWithoutContextId);
		} catch (CmisUnauthorizedException | CmisObjectNotFoundException | MalformedURLException e1) {
			logger.info(e1.getMessage());
		}

		String actualAction = (String) args.getArgumentValue(ACTION);
		String commitMessage = (String) args.getArgumentValue(COMMIT_MESSAGE);
		String actualState = (String) args.getArgumentValue(STATE);

		logger.info(" actualAction: " + actualAction);
		logger.info(" actualState: " + actualState);

		if (!actualAction.isEmpty()) {
			actionManipulator(actualAction, actualState, commitMessage, urlWithoutContextId, authorAccess, model);
		}

		if (oldVersionJson != null) {
			return oldVersionJson;
		}

		return errorInformation;
	}

  private String getUrlWithoutContextId(AuthorAccess authorAccess) {
    WebappPluginWorkspace workspace = (WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace();
		SessionStore sessionStore = workspace.getSessionStore();

		// Get URL and ContextID and create new instance of CmisURLConnection
		URL url = authorAccess.getEditorAccess().getEditorLocation();
		String urlWithoutContextId = URLStreamHandlerWithContextUtil.getInstance().toStrippedExternalForm(url);
		String contextId = url.getUserInfo();
		credentials = sessionStore.get(contextId, "wa-cmis-plugin-credentials");
		connection = new CmisURLConnection(url, new CMISAccess(), credentials);

		if (urlWithoutContextId.contains(OLD_VERSION) || urlWithoutContextId.contains("?")) {
			urlWithoutContextId = urlWithoutContextId.substring(0, urlWithoutContextId.indexOf('?'));
		}
    return urlWithoutContextId;
  }
}
