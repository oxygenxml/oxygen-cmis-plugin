package com.oxygenxml.cmis.web.action;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.CredentialsManager;

import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContextUtil;


public class CmisActionsUtills {
	
	private static final Logger logger = Logger.getLogger(CmisActionsUtills.class.getName());

	/**
	 * Not meant to be instantiated.
	 */
	private CmisActionsUtills() {}
	
	/**
	 * JSON Builder for error details.
	 * 
	 * @param errorType
	 * @param errorInfo
	 * @return Error info JSON String.
	 * @throws IOException 
	 */
	private static String getErrorInfoJSON(String errorType, String errorInfo) throws IOException {	
		HashMap<String, String> errorMap = new HashMap<>();
		errorMap.put("error", errorType);
		
		if (errorInfo != null && !errorMap.isEmpty()) {
			errorMap.put("message", errorInfo);
		}
		
		return new ObjectMapper().writeValueAsString(errorMap);
	}

	/**
	 * Get JSON result from errorInfoBuilder,
	 * Handle exceptions if it's thrown.
	 * 
	 * @param errorType
	 * @param errorMessage
	 * @return the JSON string 
	 */
	public static String returnErrorInfoJSON(String errorType, String errorMessage) {
		String errorInfoJSON = null;
		
		try {
			errorInfoJSON = CmisActionsUtills.getErrorInfoJSON(errorType, errorMessage);
		} catch (IOException e) {
			logger.debug(e.getMessage());
		}
		
		return errorInfoJSON;
	}
	
	/**
	 * Open connection for CMIS operations.
	 * 
	 * @param url
	 * @return CmisURLConnetion for AuthorOperations.
	 */
	public static CmisURLConnection getCmisURLConnection(URL url)  {
		String contextId = url.getUserInfo();
		UserCredentials credentials = CredentialsManager.INSTANCE.getCredentials(contextId);
		
		logger.info("Getting connection!");
		
		return new CmisURLConnection(url, new CMISAccess(), credentials);
	}
	
	/**
	 * Removing Context Id from URL.
	 * 
	 * @param url
	 * @return URL as String without Context Id.
	 */
	public static String getUrlWithoutContextId(URL url) {
   
		String urlWithoutContextId = URLStreamHandlerWithContextUtil.getInstance().toStrippedExternalForm(url);
	
		if (urlWithoutContextId.contains(CmisAction.OLD_VERSION.getValue()) || urlWithoutContextId.contains("?")) {
			urlWithoutContextId = urlWithoutContextId.substring(0, urlWithoutContextId.indexOf('?'));
		}
		
		return urlWithoutContextId;
  }
	
  public static Document getLatestVersion(Document document) {
    Document latest = document;
    if (!Boolean.TRUE.equals(document.isLatestVersion())) {
      latest = document.getObjectOfLatestVersion(false);
    }
    return latest;
  }
}
