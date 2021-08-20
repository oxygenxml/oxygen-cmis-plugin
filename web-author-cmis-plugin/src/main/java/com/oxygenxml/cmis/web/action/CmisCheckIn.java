package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
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

	
	private Document document;
	
	/**
	 * Do CMIS Check in operation.
	 * 
	 */
	@Override
	public String doOperation(AuthorDocumentModel model, ArgumentsMap args)
			throws AuthorOperationException {

		AuthorAccess authorAccess = model.getAuthorAccess();
		authorAccess.getWorkspaceAccess();
		
		URL url = authorAccess.getEditorAccess().getEditorLocation();

		CmisURLConnection connection = CmisActionsUtills.getCmisURLConnection(url);
		
		// Get Session Store
		String urlWithoutContextId = CmisActionsUtills.getUrlWithoutContextId(url);
		
		try {
			document = (Document) connection.getCMISObject(urlWithoutContextId);
		} catch (CmisUnauthorizedException | CmisObjectNotFoundException | MalformedURLException e) {
			logger.debug(e.getStackTrace());
		}
		
		String actualAction = (String) args.getArgumentValue(CmisAction.ACTION.getValue());
		String commitMessage = (String) args.getArgumentValue(CmisAction.COMMIT_MESSAGE.getValue());
		String actualState = (String) args.getArgumentValue(CmisAction.STATE.getValue());
		
		if (!actualAction.isEmpty() && actualAction.equals(CmisAction.CHECK_IN.getValue())) {
			
			PluginResourceBundle rb = ((WebappPluginWorkspace) PluginWorkspaceProvider
						.getPluginWorkspace()).getResourceBundle();
			
			try {
				checkInDocument(document, actualState, commitMessage);
				
				if (EditorListener.isCheckOutRequired()) {
					authorAccess.getEditorAccess()
							.setReadOnly(new ReadOnlyReason(rb.getMessage(TranslationTags.CHECK_OUT_REQUIRED)));
				}
				
			} catch (Exception e) {
				return CmisActionsUtills.returnErrorInfoJSON("denied", e.getMessage());
			}
			
		}
		
		return CmisActionsUtills.returnErrorInfoJSON("no_error", null);
	}
	 
	/**
	 * Check in the obtained CMIS Document.
	 * 
	 * @param document
	 * @param actualState
	 * @param commitMessage
	 */
	public static void checkInDocument(Document document, String actualState,
			String commitMessage) {

    Document latest = CmisActionsUtills.getLatestVersion(document);

    if (document.isVersionSeriesCheckedOut()) {

      // If commit message is null or value is "null" - assign empty string.
      if (commitMessage == null || commitMessage == "null") {
        commitMessage = "";
      }

      if (actualState.equals(CmisAction.MAJOR_STATE.getValue())) {
        latest.checkIn(true, null, null, commitMessage);
      } else {
        latest.checkIn(false, null, null, commitMessage);
      }

      latest.refresh();
      logger.info(latest.getName() + " checked-out: " + latest.isVersionSeriesCheckedOut());
    } else {
      logger.info("Document isn't checked-out!");

    }
	}
}
