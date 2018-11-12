package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import ro.sync.basic.util.URLUtil;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorOperationWithResult;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;

@WebappRestSafe
public class CmisOldVersions extends AuthorOperationWithResult {
	private static final Logger logger = Logger.getLogger(CmisOldVersions.class.getName());

	private static final String LIST_VERSIONS = "listOldVersions";
	private static final String ACTION = "action";
	
	private String oldVersionJSON;
	
	private CmisURLConnection connection;
	private Document document;
	
	/**
	 * Do CMIS Old Versions operation.
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
		
		String actualAction = (String) args.getArgumentValue(ACTION);
		
		if (!actualAction.isEmpty() && actualAction.equals(LIST_VERSIONS)) {
			
			try {
				oldVersionJSON = listOldVersions(document, urlWithoutContextId);
				
				if(oldVersionJSON != null && !oldVersionJSON.isEmpty()) {
					return oldVersionJSON;
				}
				
			} catch (Exception e) {
				return CmisActionsUtills.errorInfoBuilder("denied", e.getMessage());
			}
		}
		 
		return CmisActionsUtills.errorInfoBuilder("no_error", null);
	}
	
	/**
	 * Create JSON String with old versions of document.
	 * Put in JSON the URL to this older documents.
	 * 
	 * @param document
	 * @param url
	 * @return
	 */
	public static String listOldVersions(Document document, String url) {
		
		if (url.contains(CmisActionsUtills.OLD_VERSION)) {
			url = url.substring(0, url.indexOf('?'));
		}

		document = document.getObjectOfLatestVersion(false);

		List<Document> oldVersionsList = document.getAllVersions();
		oldVersionsList.remove(oldVersionsList.size() - 1);

		StringBuilder oldBuilder = new StringBuilder();

		oldBuilder.append("{");
		
		if(document.isVersionSeriesCheckedOut()) {
			oldVersionsList.remove(0);
		}

		for (Document oldDoc : oldVersionsList) {
			if (oldDoc.getVersionLabel().equals("pwc")) {
				continue;
			}
			
			if(oldDoc.isLatestVersion()){
				oldBuilder.append("\"").append("v" + oldDoc.getVersionLabel()).append("\"");
				oldBuilder.append(":").append("[").append("\"");
				oldBuilder.append("?url=").append(URLUtil.encodeURIComponent(url)).append("\"");
			} else {
				oldBuilder.append("\"").append("v" + oldDoc.getVersionLabel()).append("\"");
				oldBuilder.append(":").append("[");
				oldBuilder.append("\"").append("?url=").append(URLUtil.encodeURIComponent(url));
				oldBuilder.append("?oldversion=").append(oldDoc.getId()).append("\"");
			}
			
			String checkInComment = null;

			if (oldDoc.getCheckinComment() == null) {
				checkInComment = "";
			} else {
				checkInComment = oldDoc.getCheckinComment();
				checkInComment = checkInComment.replaceAll("\\n", "<br/>");
			}

			oldBuilder.append(",").append("\"").append(checkInComment).append("\"");
			oldBuilder.append(",").append("\"").append(oldDoc.getLastModifiedBy());
			oldBuilder.append("\"").append("]");
			oldBuilder.append(",");
		}

		oldBuilder.replace(oldBuilder.lastIndexOf(","), oldBuilder.lastIndexOf(",") + 1, "");
		oldBuilder.append("}");

		logger.info(oldBuilder.toString());
		
		return oldBuilder.toString();
	}
	
}
