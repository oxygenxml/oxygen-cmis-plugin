package com.oxygenxml.cmis.web.action;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

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
	
	private Document document;
	
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
		
		try {
			document = (Document) connection.getCMISObject(urlWithoutContextId);
		} catch (CmisUnauthorizedException | CmisObjectNotFoundException | MalformedURLException e) {
			logger.debug(e.getStackTrace());
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
	 * @param currentVersion 
	 * @return
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
		HashMap<String, List<String>> versionMap = new LinkedHashMap<>();
		
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
			
			versionMap.put(label, createProps(urlParam, commitMessage, authorName));
		}
		return new ObjectMapper().writeValueAsString(versionMap);
	}
	
	/**
	 * Create the properties for a version, currently a list of strings.
	 * 
	 * @param urlParam
	 * @param commitMessage
	 * @param authorName
	 * @return
	 */
	private static List<String> createProps(String urlParam, String commitMessage, String authorName) {
    return Arrays.asList(urlParam, commitMessage, authorName);	  
	}
	
}
