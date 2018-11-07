package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.EditorListener;
import com.oxygenxml.cmis.web.TranslationTags;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorOperationWithResult;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.ReadOnlyReason;

@WebappRestSafe
public class CmisCheckIn extends AuthorOperationWithResult{
	private static final Logger logger = Logger.getLogger(CmisCheckIn.class.getName());

	private static final String CHECK_IN = "cmisCheckin";
	private static final String COMMIT_MESSAGE = "commit";
	private static final String STATE = "state";
	private static final String MAJOR_STATE = "major";
	private static final String ACTION = "action";
	
	private CmisURLConnection connection;
	private Document document;
	
	/**
	 * Do CMIS Check in operation.
	 */
	@Override
	public String doOperation(AuthorDocumentModel model, ArgumentsMap args)
			throws IllegalArgumentException, AuthorOperationException {

		logger.info("i'm here");
		
		AuthorAccess authorAccess = model.getAuthorAccess();
		authorAccess.getWorkspaceAccess();
		
		URL url = authorAccess.getEditorAccess().getEditorLocation();

		connection = CmisActionsUtills.getCmisURLConnection(url);
		
		// Get Session Store
		String urlWithoutContextId = CmisActionsUtills.getUrlWithoutContextId(url);
		
		try {
			document = (Document) connection.getCMISObject(urlWithoutContextId);
		} catch (CmisUnauthorizedException | CmisObjectNotFoundException | MalformedURLException e) {
			logger.debug(e.getStackTrace());
		}
		
		String actualAction = (String) args.getArgumentValue(ACTION);
		String commitMessage = (String) args.getArgumentValue(COMMIT_MESSAGE);
		String actualState = (String) args.getArgumentValue(STATE);
		
		if (!actualAction.isEmpty() && actualAction.equals(CHECK_IN)) {
			
			PluginResourceBundle rb = ((WebappPluginWorkspace) PluginWorkspaceProvider
						.getPluginWorkspace()).getResourceBundle();
			
			try {
				Session session = connection.getCMISAccess().getSession();
				checkInDocument(document, session, actualState, commitMessage);
				
				if (EditorListener.isCheckOutRequired()) {
					authorAccess.getEditorAccess()
							.setReadOnly(new ReadOnlyReason(rb.getMessage(TranslationTags.CHECK_OUT_REQUIRED)));
				}
				
			} catch (Exception e) {
				return CmisActionsUtills.errorInfoBuilder("denied", e.getMessage());
			}
			
		}
		
		return CmisActionsUtills.errorInfoBuilder("no_error", null);
	}
	
	/**
	 * Check in the obtained CMIS Document.
	 * 
	 * @param document
	 * @param session
	 * @param actualState
	 * @param commitMessage
	 * @throws Exception
	 */
	public static void checkInDocument(Document document, Session session, String actualState,
			String commitMessage) throws Exception {

		if (!document.isVersionSeriesCheckedOut()) {
			logger.info("Document isn't checked-out!");

		} else {
			document = document.getObjectOfLatestVersion(false);
			String pwc = document.getVersionSeriesCheckedOutId();

			if (pwc != null) {
				Document PWC = (Document) session.getObject(pwc);

				if (commitMessage == null || commitMessage == "null") {
					commitMessage = "";
				}

				if (actualState.equals(MAJOR_STATE)) {
					PWC.checkIn(true, null, null, commitMessage);
				} else {
					PWC.checkIn(false, null, null, commitMessage);
				}
			}

			document.refresh();
			logger.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());
		}
	}
}
