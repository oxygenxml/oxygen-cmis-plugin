package com.oxygenxml.cmis.web;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.CmisURL;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.WebappMessage;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContext;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;
import ro.sync.ecss.extensions.api.webapp.plugin.UserContext;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CmisStreamHandler extends URLStreamHandlerWithContext {

	private static final Logger logger = Logger.getLogger(CmisStreamHandler.class.getName());

  @Override
  protected String getContextId(UserContext context) {
    String sessionId = context.getSessionId();
    String contextId = String.valueOf(sessionId.hashCode());
    contextIdToSessionIdMap.put(contextId, sessionId);
    return contextId;
  }

  /**
   * Map from context id to session id.
   */
  public static final Cache<String, String> contextIdToSessionIdMap = 
     CacheBuilder.newBuilder()
       .concurrencyLevel(10)
       .maximumSize(10000)
       .build();

	@Override
	protected URLConnection openConnectionInContext(String contextId, URL url, Proxy proxy) throws IOException {
		// Accessing webapp to get credentials
		WebappPluginWorkspace workspace = (WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace();
		SessionStore sessionStore = workspace.getSessionStore();

		// Getting credentials and another information
    String sessionId = contextIdToSessionIdMap.getIfPresent(contextId);
    if (sessionId == null) {
      WebappMessage webappMessage = new WebappMessage(WebappMessage.MESSAGE_TYPE_ERROR, "401",
        "Invalid session.", true);
      throw new UserActionRequiredException(webappMessage);
    }
		UserCredentials credentials = sessionStore.get(sessionId, "wa-cmis-plugin-credentials");
		CMISAccess cmisAccess = new CMISAccess();
		CmisURLConnection cuc = new CmisURLConnection(url, cmisAccess, credentials);
		URL serverUrl = CmisURL.parseServerUrl(url.toExternalForm());

		logger.info("Server URL: " + serverUrl.toExternalForm());

		boolean isUserValid = true;
		if (credentials != null && !credentials.isEmpty()) {
			try {
				cmisAccess.pureConnectToServer(serverUrl, credentials);
			} catch (Exception e) {
			  // may be CmisUnauthorizedException
			  logger.error(e, e);
			  isUserValid = false;
			}
		} else {
		  isUserValid = false;
		}
		
		if (!isUserValid) {
		  WebappMessage webappMessage = new WebappMessage(WebappMessage.MESSAGE_TYPE_ERROR, "401",
		      "Invalid username or password!", true);
		  throw new UserActionRequiredException(webappMessage);
		}

		return new CmisBrowsingURLConnection(cuc, serverUrl);
	}

}
