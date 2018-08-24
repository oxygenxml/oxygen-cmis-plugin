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

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContextUtil;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.ReadOnlyReason;

@WebappRestSafe
public class CmisActions implements AuthorOperation {
	private static final Logger logger = Logger.getLogger(CmisActions.class.getName());

	private CmisURLConnection connection;
	private UserCredentials credentials;
	private Document document;

	private static final String CHECK_OUT = "cmisCheckout";
	private static final String CHECK_IN = "cmisCheckin";
	private static final String CANCEL_CHECK_OUT = "cancelCmisCheckout";
	private static final String COMMIT_MESSAGE = "commit";
	private static final String ACTION = "action";
	private static final String STATE = "state";

	@Override
	public String getDescription() {
		return "";
	}

	private void actionManipulator(String actualAction, String actualState, String commitMessage,
			AuthorAccess authorAccess) {

		try {
			if (actualAction.equals(CHECK_OUT)) {
				CmisCheckOutAction.checkOutDocument(document, connection);
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
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Invalid object or object URL!");
		}

	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args)
			throws IllegalArgumentException, AuthorOperationException {
		
		authorAccess.getWorkspaceAccess();

		// Get Session Store
		WebappPluginWorkspace workspace = (WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace();
		SessionStore sessionStore = workspace.getSessionStore();

		// Get URL and ContextID and create new instance of CmisURLConnection
		URL url = authorAccess.getEditorAccess().getEditorLocation();
		String urlWithoutContextId = URLStreamHandlerWithContextUtil.getInstance().toStrippedExternalForm(url);
		String contextId = url.getUserInfo();
		credentials = sessionStore.get(contextId, "credentials");
		connection = new CmisURLConnection(url, new CMISAccess(), credentials);

		try {
			document = (Document) connection.getCMISObject(urlWithoutContextId);
		} catch (CmisUnauthorizedException e1) {
			e1.printStackTrace();
		} catch (CmisObjectNotFoundException e1) {
			e1.printStackTrace();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		logger.info("Is versionable: " + document.isVersionable());

		String actualAction = (String) args.getArgumentValue(ACTION);
		String commitMessage = (String) args.getArgumentValue(COMMIT_MESSAGE);
		String actualState = (String) args.getArgumentValue(STATE);

		logger.info(" actualAction: " + actualAction);
		logger.info(" actualState: " + actualState);

		if (!actualAction.isEmpty()) {
			actionManipulator(actualAction, actualState, commitMessage, authorAccess);
		}

	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}
}
