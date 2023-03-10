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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.TranslationTags;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CmisOldVersions extends AuthorOperationWithResult {
	
	/**
	 * Do CMIS Old Versions operation.
	 */
	@Override
	public String doOperation(AuthorDocumentModel model, ArgumentsMap args)
			throws AuthorOperationException {

	  String oldVersionJSON;
	  CmisURLConnection connection;
	  
		AuthorAccess authorAccess = model.getAuthorAccess();
		authorAccess.getWorkspaceAccess();

		URL url = authorAccess.getEditorAccess().getEditorLocation();

		connection = CmisActionsUtills.getCmisURLConnection(url);
		
		// Get Session Store
		String urlWithoutContextIdAndVersion = CmisActionsUtills.getUrlWithoutContextIdAndVersion(url);
		
		Document document = null;
		try {
			document = (Document) connection.getCMISObject(urlWithoutContextIdAndVersion);
		} catch (CmisUnauthorizedException | CmisObjectNotFoundException | MalformedURLException e) {
			log.debug("Error getting CMIS document " + urlWithoutContextIdAndVersion);
			throw(new AuthorOperationException(e.getMessage()));
		}
		
		String actualAction = (String) args.getArgumentValue(CmisAction.ACTION.getValue());
		
		if (!actualAction.isEmpty() && actualAction.equals(CmisAction.LIST_VERSIONS.getValue())) {
			
			try {
				List<Map<String, String>> allVersions = listOldVersions(document, urlWithoutContextIdAndVersion);
				oldVersionJSON = new ObjectMapper().writeValueAsString(allVersions);
				
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
	 * @param document The CMIS document.
	 * @param url The OXY URL.
	 * 
	 * @return the list of versions
	 * @throws IOException 
	 */
	public static List<Map<String, String>> listOldVersions(
	    Document document, String url)
	    throws IOException {
	  PluginResourceBundle rb = ((WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();

		document = document.getObjectOfLatestVersion(false);		
		List<Document> allVersions = document.getAllVersions();
		
		List<Map<String, String>> versions = new ArrayList<>();
		
		boolean isCheckedOut = document.isVersionSeriesCheckedOut();
		for (int i = 0; i < allVersions.size(); i++) {
			Document version = allVersions.get(i);
			// Check if server support Private Working Copies.
			boolean isCurrentVersion = Boolean.TRUE.equals(version.isPrivateWorkingCopy()) || i == 0; 
			
			String label = isCurrentVersion && isCheckedOut ? rb.getMessage(TranslationTags.CURRENT) : "v" + version.getVersionLabel();
			String urlParam = "?url=" + URLUtil.encodeURIComponent(url);
			if (!isCurrentVersion) {
			  urlParam += "?oldversion=" + version.getId();
			}
			String commitMessage = version.getCheckinComment() != null ? version.getCheckinComment() : "";
			String authorName = version.getLastModifiedBy();
			
			versions.add(createProps(label, urlParam, commitMessage, authorName));
		}
		return versions;
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
