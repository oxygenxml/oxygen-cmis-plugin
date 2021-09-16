package com.oxygenxml.cmis.web.action;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.TranslationTags;

import ro.sync.basic.util.URLUtil;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorOperationWithResult;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

@WebappRestSafe
public class CmisOldVersions extends AuthorOperationWithResult {
	
	private static final Logger logger = Logger.getLogger(CmisOldVersions.class.getName());
	
	/**
	 * Do CMIS Old Versions operation.
	 */
	@Override
	public String doOperation(AuthorDocumentModel model, ArgumentsMap args)
			throws AuthorOperationException {

	  /**
	   * JSON String result of operation. 
	   */
	  String oldVersionJSON;
	  CmisURLConnection connection;
	  
		AuthorAccess authorAccess = model.getAuthorAccess();
		authorAccess.getWorkspaceAccess();

		URL url = authorAccess.getEditorAccess().getEditorLocation();

		connection = CmisActionsUtills.getCmisURLConnection(url);
		
		// Get Session Store
		String urlWithoutContextId = CmisActionsUtills.getUrlWithoutContextId(url);
		
		Document document = null;
		try {
			document = (Document) connection.getCMISObject(urlWithoutContextId);
		} catch (CmisUnauthorizedException | CmisObjectNotFoundException | MalformedURLException e) {
			logger.debug("Error getting CMIS document " + urlWithoutContextId);
			throw(new AuthorOperationException(e.getMessage()));
		}
		
		String actualAction = (String) args.getArgumentValue(CmisAction.ACTION.getValue());
		
		if (!actualAction.isEmpty() && actualAction.equals(CmisAction.LIST_VERSIONS.getValue())) {
			
			try {
				PluginResourceBundle rb = ((WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();
				String currentVersion = rb.getMessage(TranslationTags.CURRENT);
				
				oldVersionJSON = listOldVersions(document, urlWithoutContextId, currentVersion);
				
				if(oldVersionJSON != null && !oldVersionJSON.isEmpty()) {
					return oldVersionJSON;
				}
				
			} catch (Exception e) {
				return CmisActionsUtills.returnErrorInfoJSON("denied", e.getMessage());
			}
		}
		 
		return CmisActionsUtills.returnErrorInfoJSON("no_error", null);
	}
	
	/**
	 * Create JSON String with old versions of document.
	 * Put in JSON the URL to this older documents.
	 * 
	 * @param document
	 * @param url
	 * @param currentVersion 
	 * @return the JSON string containing the list of versions
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public static String listOldVersions(Document document, String url, String currentVersion) throws IOException {
		
		// Removing query part of URL, in this way
		// we escape duplicates of queries.
		if (url.contains(CmisAction.OLD_VERSION.getValue())) {
			url = url.substring(0, url.indexOf('?'));
		}

		document = document.getObjectOfLatestVersion(false);		
		List<Document> allVersions = document.getAllVersions();
		
		List<Map<String, String>> versions = new ArrayList<>();
		
		boolean isCheckedOut = document.isVersionSeriesCheckedOut();
		for (int i = 0; i < allVersions.size(); i++) {
			Document version = allVersions.get(i);
			// Check if server support Private Working Copies.
			boolean isCurrentVersion = Boolean.TRUE.equals(version.isPrivateWorkingCopy()) || i == 0; 
			
			String label = isCurrentVersion && isCheckedOut ? currentVersion : "v" + version.getVersionLabel();
			String urlParam = "?url=" + URLUtil.encodeURIComponent(url);
			if (!isCurrentVersion) {
			  urlParam += "?oldversion=" + version.getId();
			}
			String commitMessage = version.getCheckinComment() != null ? version.getCheckinComment() : "";
			String authorName = version.getLastModifiedBy();
			
			versions.add(createProps(label, urlParam, commitMessage, authorName));
		}
		return new ObjectMapper().writeValueAsString(versions);
	}
	
	/**
	 * Create the properties for a version, currently a list of strings.
	 * 
	 * @param label
	 * @param urlParam
	 * @param commitMessage
	 * @param authorName
	 * @return
	 */
	private static Map<String, String> createProps(String label, String urlParam, String commitMessage, String authorName) {
	  Map<String, String> props = new HashMap<>(3);
	  props.put("version", label);
	  props.put("url", urlParam);
	  props.put("author", authorName);
	  if(commitMessage != null) {
	    props.put("commitMessage", commitMessage);
	  }
	  
    return props;
	}
	
}
