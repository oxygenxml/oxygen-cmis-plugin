package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.EditorListener;
import com.oxygenxml.cmis.web.TranslationTags;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CmisCheckIn extends AuthorOperationWithResult{

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
		Document document = null;
		try {
			document = (Document) connection.getCMISObject(urlWithoutContextId);
		} catch (CmisUnauthorizedException | CmisObjectNotFoundException | MalformedURLException e) {
		  log.debug("Error getting CMIS document " + urlWithoutContextId);
			throw(new AuthorOperationException(e.getMessage()));
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

      latest.checkIn(isMajorVersion(actualState), null, latest.getContentStream(), commitMessage);
      
      latest.refresh();
      log.info(latest.getName() + " checked-out: " + latest.isVersionSeriesCheckedOut());
    } else {
      log.info("Document isn't checked-out!");

    }
	}

  private static boolean isMajorVersion(String actualState) {
    return actualState.equals(CmisAction.MAJOR_STATE.getValue());
  }
}
