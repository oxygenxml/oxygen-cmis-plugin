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
import ro.sync.ecss.extensions.commons.operations.ReloadContentOperation;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.ReadOnlyReason;

@WebappRestSafe
public class CmisCancelCheckOut extends AuthorOperationWithResult {
	
	private static final Logger logger = Logger.getLogger(CmisCancelCheckOut.class.getName());
	
	private CmisURLConnection connection;
	private Document document;
	
	@Override
	public String doOperation(AuthorDocumentModel model, ArgumentsMap args)
			throws IllegalArgumentException, AuthorOperationException {

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
		
		String actualAction = (String) args.getArgumentValue(CmisAction.ACTION.getValue());
		
		if (!actualAction.isEmpty() && actualAction.equals(CmisAction.CANCEL_CHECK_OUT.getValue())) {
			
			PluginResourceBundle rb = ((WebappPluginWorkspace) PluginWorkspaceProvider
					.getPluginWorkspace()).getResourceBundle();
			
			try {
				Session session = connection.getCMISAccess().getSession();
				cancelCheckOutDocument(document, session);
				reloadDocument(authorAccess);
				
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
	 * Reload the document after cancel check out to discard changes.
	 * @param authorAccess
	 * @throws IllegalArgumentException
	 * @throws AuthorOperationException
	 */
	private void reloadDocument(AuthorAccess authorAccess) throws IllegalArgumentException, AuthorOperationException {
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


  public static void cancelCheckOutDocument(Document document, Session session) throws Exception {

		if (!document.isVersionSeriesCheckedOut()) {
			logger.info("Document isn't checked-out!");
			
		} else {
			document = document.getObjectOfLatestVersion(false);
			String pwc = document.getVersionSeriesCheckedOutId();

			if (pwc != null) {
				Document PWC = (Document) session.getObject(pwc);
				PWC.cancelCheckOut();
			}

			document.refresh();
			logger.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());
		}
	}
	
}
