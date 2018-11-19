package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.EditorListener;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorOperationWithResult;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;

@WebappRestSafe
public class CmisCheckOut extends AuthorOperationWithResult {
	
	private static final Logger logger = Logger.getLogger(CmisCheckOut.class.getName());

	private CmisURLConnection connection;
	private Document document;	

	/**
	 * Do CMIS Check out operation.
	 */
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
		
		if (!actualAction.isEmpty() && actualAction.equals(CmisAction.CHECK_OUT.getValue())) {
			try {
				checkOutDocument(document);
				
				if (EditorListener.isCheckOutRequired()) {
					authorAccess.getEditorAccess().setEditable(true);
				}
				
			} catch (Exception e) {
				return CmisActionsUtills.returnErrorInfoJSON("denied", e.getMessage());
			}
		}
		
		return CmisActionsUtills.returnErrorInfoJSON("no_error", null);
	}
	
	/**
	 * Check out the obtained CMIS Document.
	 * 
	 * @param document
	 * @throws Exception
	 */
	public static void checkOutDocument(Document document) throws Exception{

		document = document.getObjectOfLatestVersion(false);

		if (document.isVersionSeriesCheckedOut()) {
			logger.info("Document was checked-out!");

		} else {
			document.checkOut();
			document.refresh();
			logger.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());
		}
	}

}
