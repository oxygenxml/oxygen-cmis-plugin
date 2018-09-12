package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.EditorListener;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorOperationWithResult;
import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContextUtil;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.ReadOnlyReason;

@WebappRestSafe
public class CmisActions extends AuthorOperationWithResult {
	private static final Logger logger = Logger.getLogger(CmisActions.class.getName());

	private CmisURLConnection connection;
	private UserCredentials credentials;
	private Document document;
	
	private String oldVersionJson = null;

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

	private void actionManipulator(String actualAction, String actualState, String commitMessage,
			AuthorAccess authorAccess, String url) {

		try {
			if (actualAction.equals(CHECK_OUT)) {
				CmisCheckOutAction.checkOutDocument(document);
				if (EditorListener.isCheckOutRequired()) {
					authorAccess.getEditorAccess().setEditable(true);
				}
			}
			if (actualAction.equals(CANCEL_CHECK_OUT)) {
				CmisCheckOutAction.cancelCheckOutDocument(document, connection);
				if (EditorListener.isCheckOutRequired()) {
					authorAccess.getEditorAccess().setReadOnly(new ReadOnlyReason("Check-out required!"));
				}
			}
			if (actualAction.equals(CHECK_IN)) {
				CmisCheckInAction.checkInDocument(document, connection, actualState, commitMessage);
				if (EditorListener.isCheckOutRequired()) {
					authorAccess.getEditorAccess().setReadOnly(new ReadOnlyReason("Check-out required!"));
				}
			}
			if(actualAction.equals(LIST_VERSIONS)) {
				oldVersionJson = ListOldVersionsAction.listOldVersions(document, url);
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
			logger.info("Invalid object or object URL!");
		}
	}

	@Override
	public String doOperation(AuthorDocumentModel model, ArgumentsMap args)
			throws IllegalArgumentException, AuthorOperationException {

		AuthorAccess authorAccess = model.getAuthorAccess();
		authorAccess.getWorkspaceAccess();

		// Get Session Store
		WebappPluginWorkspace workspace = (WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace();
		SessionStore sessionStore = workspace.getSessionStore();

		// Get URL and ContextID and create new instance of CmisURLConnection
		URL url = authorAccess.getEditorAccess().getEditorLocation();
		String urlWithoutContextId = URLStreamHandlerWithContextUtil.getInstance().toStrippedExternalForm(url);
		String contextId = url.getUserInfo();
		credentials = sessionStore.get(contextId, "wa-cmis-plugin-credentials");
		connection = new CmisURLConnection(url, new CMISAccess(), credentials);

		if(urlWithoutContextId.contains(OLD_VERSION) || urlWithoutContextId.contains("?")) {
			urlWithoutContextId = urlWithoutContextId.substring(0, urlWithoutContextId.indexOf("?"));
		}
		
		try {
			document = (Document) connection.getCMISObject(urlWithoutContextId);
		} catch (CmisUnauthorizedException e1) {
			logger.info(e1.getMessage());
		} catch (CmisObjectNotFoundException e1) {
			logger.info(e1.getMessage());
		} catch (MalformedURLException e1) {
			logger.info(e1.getMessage());
		}

		String actualAction = (String) args.getArgumentValue(ACTION);
		String commitMessage = (String) args.getArgumentValue(COMMIT_MESSAGE);
		String actualState = (String) args.getArgumentValue(STATE);

		logger.info(" actualAction: " + actualAction);
		logger.info(" actualState: " + actualState);

		if (!actualAction.isEmpty()) {
			actionManipulator(actualAction, actualState, commitMessage, authorAccess, urlWithoutContextId);
		}
				
		if(oldVersionJson != null) {
			return oldVersionJson;
		}
		return null;
	}
}
