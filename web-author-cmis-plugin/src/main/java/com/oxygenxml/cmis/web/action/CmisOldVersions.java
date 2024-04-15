package com.oxygenxml.cmis.web.action;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oxygenxml.cmis.core.CmisCredentials;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.CredentialsManager;
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
		CmisCredentials currentUser = CredentialsManager.INSTANCE.getCredentials(url.getUserInfo());

		connection = CmisActionsUtills.getCmisURLConnection(url);
		
		// Get Session Store
		URL urlWithoutContextId = CmisActionsUtills.getUrlWithoutContextId(url);
		URL urlWithoutContextIdAndVersion = CmisActionsUtills.stripVersion(urlWithoutContextId);
		
		Document document = null;
		try {
			document = (Document) connection.getCMISObject(urlWithoutContextIdAndVersion.toExternalForm());
		} catch (CmisUnauthorizedException | CmisObjectNotFoundException | MalformedURLException e) {
			log.debug("Error getting CMIS document " + urlWithoutContextIdAndVersion);
			throw(new AuthorOperationException(e.getMessage()));
		}
		
		try {
			List<Map<String, String>> allVersions = listOldVersions(document, urlWithoutContextId, currentUser);
			oldVersionJSON = new ObjectMapper().writeValueAsString(allVersions);
			
			if(oldVersionJSON != null && !oldVersionJSON.isEmpty()) {
				return oldVersionJSON;
			}
			
		} catch (Exception e) {
			return CmisActionsUtills.returnErrorInfoJSON("denied", e.getMessage());
		}
		 
		return CmisActionsUtills.returnErrorInfoJSON("no_error", null);
	}
	
	/**
	 * Create JSON String with old versions of document.
	 * Put in JSON the URL to this older documents.
	 * 
	 * @param document The CMIS document.
	 * @param currentDocUrl The current OXY URL without credentials.
	 * @param currentUser The current user.
	 * 
	 * @return the list of versions
	 * @throws IOException If it fails.
	 */
	public static List<Map<String, String>> listOldVersions(
	    Document document, URL currentDocUrl, CmisCredentials currentUser)
	    throws IOException {
	  PluginResourceBundle rb = ((WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();

	  Optional<String> currentDocVersion = CmisActionsUtills.getVersionId(currentDocUrl);
	  String docUrlWithoutVersion = CmisActionsUtills.stripVersion(currentDocUrl).toExternalForm();
		List<Document> allVersions = document.getObjectOfLatestVersion(false).getAllVersions();
		
		List<Map<String, String>> versions = new ArrayList<>();
		
		for (int i = 0; i < allVersions.size(); i++) {
			Document version = allVersions.get(i);
			String versionId = version.getId();

			boolean isPwcVersion = Boolean.TRUE.equals(version.isPrivateWorkingCopy()) || Boolean.TRUE.equals(version.isVersionSeriesPrivateWorkingCopy());
			boolean isCheckedOutByMe = currentUser.getUsername().equals(document.getVersionSeriesCheckedOutBy());
			boolean isCurrentVersion;
			if (currentDocVersion.isPresent()) {
				isCurrentVersion = version.getId().equals(currentDocVersion.get());
			} else {
				isCurrentVersion = isPwcVersion && isCheckedOutByMe;
			}
			
			String label;
			if (isPwcVersion) {
				label = isCurrentVersion ? rb.getMessage(TranslationTags.CURRENT) : "PWC";
			} else {
				label = version.getVersionLabel();
			}
			String urlParam;
			if (isPwcVersion) {
				urlParam = docUrlWithoutVersion;
			} else {
				urlParam = docUrlWithoutVersion + "?oldversion=" + versionId;
			}
			String commitMessage = version.getCheckinComment() != null ? version.getCheckinComment() : "";
			String authorName = version.getLastModifiedBy();
			versions.add(createProps(label, urlParam, commitMessage, authorName, isCurrentVersion));
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
	private static Map<String, String> createProps(String label, String urlParam, String commitMessage, String authorName, boolean isCurrentVersion) {
	  Map<String, String> props = new HashMap<>(3);
	  props.put("version", label);
	  props.put("url", urlParam);
	  props.put("author", authorName);
	  if(commitMessage != null) {
	    props.put("commitMessage", commitMessage);
	  }
	  props.put("isCurrentVersion", String.valueOf(isCurrentVersion));
	  
    return props;
	}
	
}
