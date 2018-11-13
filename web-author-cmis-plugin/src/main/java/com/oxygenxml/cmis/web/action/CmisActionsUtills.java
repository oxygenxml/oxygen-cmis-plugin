package com.oxygenxml.cmis.web.action;

import java.net.URL;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContextUtil;
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
	 */
	public static String errorInfoBuilder(String errorType, String errorInfo) {
		StringBuilder infoBuilder = new StringBuilder();

		infoBuilder.append("{");
		infoBuilder.append("\"error\"").append(":");
		infoBuilder.append("\"" + errorType + "\"");
		
		if(errorInfo != null) {
			infoBuilder.append(",");
			infoBuilder.append("\"message\"").append(":");
			infoBuilder.append("\"" + errorInfo + "\"");
		}
		
		infoBuilder.append("}");
		
		return infoBuilder.toString();
	}

	/**
	 * Open connection for CMIS operations.
	 * 
	 * @param url
	 * @return CmisURLConnetion for AuthorOperations.
	 */
	public static CmisURLConnection getCmisURLConnection(URL url) {
		
		WebappPluginWorkspace workspace = (WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace();
		SessionStore sessionStore = workspace.getSessionStore();
		String contextId = url.getUserInfo();
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
