package com.oxygenxml.cmis.web.action;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.CmisStreamHandler;

import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.WebappMessage;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContextUtil;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;


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
	 * @return
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
		WebappPluginWorkspace workspace = (WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace();
		SessionStore sessionStore = workspace.getSessionStore();
		String contextId = url.getUserInfo();
    String sessionId = CmisStreamHandler.contextIdToSessionIdMap.getIfPresent(contextId);
    if (sessionId == null) {
      throw new IllegalArgumentException("Invalid session.");
    }
		UserCredentials credentials = sessionStore.get(contextId, "wa-cmis-plugin-credentials");
		
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
}
