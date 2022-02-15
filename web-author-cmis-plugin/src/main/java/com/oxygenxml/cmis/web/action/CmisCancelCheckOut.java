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
import ro.sync.ecss.extensions.commons.operations.ReloadContentOperation;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.ReadOnlyReason;

@WebappRestSafe
@Slf4j
public class CmisCancelCheckOut extends AuthorOperationWithResult {
	
	@Override
	public String doOperation(AuthorDocumentModel model, ArgumentsMap args)
			throws AuthorOperationException {

	  Document document;
	  CmisURLConnection connection;
	  
		AuthorAccess authorAccess = model.getAuthorAccess();
		authorAccess.getWorkspaceAccess();

		URL url = authorAccess.getEditorAccess().getEditorLocation();

		connection = CmisActionsUtills.getCmisURLConnection(url);
		
		// Get Session Store
		String urlWithoutContextId = CmisActionsUtills.getUrlWithoutContextId(url);
		
		try {
			document = (Document) connection.getCMISObject(urlWithoutContextId);
		} catch (CmisUnauthorizedException | CmisObjectNotFoundException | MalformedURLException e) {
			log.debug(e.getStackTrace());
			throw(new AuthorOperationException(e.getMessage()));
		}
		
		String actualAction = (String) args.getArgumentValue(CmisAction.ACTION.getValue());
		
		if (!actualAction.isEmpty() && actualAction.equals(CmisAction.CANCEL_CHECK_OUT.getValue())) {
			
			PluginResourceBundle rb = ((WebappPluginWorkspace) PluginWorkspaceProvider
					.getPluginWorkspace()).getResourceBundle();
			
			try {
				cancelCheckOutDocument(document);
				reloadDocument(authorAccess);
				
				if (EditorListener.isCheckOutRequired()) {
					authorAccess.getEditorAccess()
							.setReadOnly(new ReadOnlyReason(rb.getMessage(TranslationTags.CHECK_OUT_REQUIRED)));
				}
				
			} catch (Exception e) {
			  log.error("Could not cancel check out for document:" + document, e);
				return CmisActionsUtills.returnErrorInfoJSON("denied", e.getMessage());
			}
		}

		return CmisActionsUtills.returnErrorInfoJSON("no_error", null);
	}
	
	/**
	 * Reload the document after cancel check out to discard changes.
	 * @param authorAccess
	 * @throws AuthorOperationException
	 */
	private void reloadDocument(AuthorAccess authorAccess) throws AuthorOperationException {
	  ReloadContentOperation reloadAction = new ReloadContentOperation();
    class ActionIdArgumentsMap implements ArgumentsMap {
      @Override
      public Object getArgumentValue(String argumentName) {
        return argumentName.equals("markAsNotModified") ? true : "";
      }
    }
    ArgumentsMap argMap = new ActionIdArgumentsMap();
    reloadAction.doOperation(authorAccess, argMap);
  }
	
  public static void cancelCheckOutDocument(Document document) {
    Document latest = document.getObjectOfLatestVersion(false);
    
		if (latest.isVersionSeriesCheckedOut()) {
		  latest.cancelCheckOut();

      log.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());
		} else {
		  log.info("Document isn't checked-out!");
			
		}
	}
	
}
