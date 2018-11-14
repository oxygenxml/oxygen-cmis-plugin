package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.TranslationTags;

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
	
	/**
	 * JSON String result of operation. 
	 */
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
		
		String actualAction = (String) args.getArgumentValue(CmisAction.ACTION.getValue());
		
		if (!actualAction.isEmpty() && actualAction.equals(CmisAction.LIST_VERSIONS.getValue())) {
			
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
		
		StringBuilder builder = new StringBuilder();
		// Removing query part of URL, in this way
		// we escape duplicates of queries.
		if (url.contains(CmisAction.OLD_VERSION.getValue())) {
			url = url.substring(0, url.indexOf('?'));
		}

		document = document.getObjectOfLatestVersion(false);		
		List<Document> allVersions = document.getAllVersions();
		
		boolean isCheckedOut = document.isVersionSeriesCheckedOut();

		builder.append("{");
		
		for (int i = 0; i < allVersions.size(); i++) {
			Document version = allVersions.get(i);
			String label = "v" + version.getVersionLabel();
			// Check if server support Private Working Copies.
			// If PWC is supported we add it builder.
			if (Boolean.TRUE.equals(version.isPrivateWorkingCopy()) || i == 0) {
				label = isCheckedOut ? TranslationTags.CURRENT : label; 
				
				builder.append("\"").append(label).append("\"");
				builder.append(":").append("[").append("\"");
				builder.append("?url=").append(URLUtil.encodeURIComponent(url)).append("\"");
			} else {
				builder.append("\"").append(label).append("\"");
				builder.append(":").append("[");
				builder.append("\"").append("?url=").append(URLUtil.encodeURIComponent(url));
				builder.append("?oldversion=").append(version.getId()).append("\"");
			}
			
			String comment = null;

			if (version.getCheckinComment() == null) {
				comment = "";
			} else {
				comment = version.getCheckinComment();
				comment = comment.replaceAll("\\n", "<br/>");
			}

			builder.append(",").append("\"").append(comment).append("\"");
			builder.append(",").append("\"").append(version.getLastModifiedBy());
			builder.append("\"").append("]");
			builder.append(",");
		}

		builder.replace(builder.lastIndexOf(","), builder.lastIndexOf(",") + 1, "");
		builder.append("}");

		logger.info(builder.toString());
		
		return builder.toString();
	}
	
}
