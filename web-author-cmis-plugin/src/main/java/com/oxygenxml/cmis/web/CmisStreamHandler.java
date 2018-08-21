package com.oxygenxml.cmis.web;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.WebappMessage;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContext;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CmisStreamHandler extends URLStreamHandlerWithContext {

	private static final Logger logger = Logger.getLogger(CmisStreamHandler.class.getName());

	@Override
	protected URLConnection openConnectionInContext(String contextId, URL url, Proxy proxy) throws IOException {
		// Accessing webapp to get credentials
		WebappPluginWorkspace workspace = (WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace();
		SessionStore sessionStore = workspace.getSessionStore();

		// Getting credentials and another information
		UserCredentials credentials = sessionStore.get(contextId, "credentials");
		CMISAccess cmisAccess = new CMISAccess();
		CmisURLConnection cuc = new CmisURLConnection(url, cmisAccess, credentials);
		URL serverUrl = CmisURLConnection.getServerURL(url.toExternalForm(), null);

		logger.info("Server URL = " + serverUrl.toExternalForm());

		WebappMessage webappMessage = new WebappMessage(2, "401", "Invalid username or password!", true);

		if (credentials != null && !credentials.getPassword().isEmpty() && !credentials.getUsername().isEmpty()) {
			logger.info(credentials.toString());

			try {
				cmisAccess.pureConnectToServer(serverUrl, credentials);
			} catch (CmisUnauthorizedException e) {
				logger.info("getInputStream() ---> " + e.toString());
				throw new UserActionRequiredException(webappMessage);
			}
		} else {
			throw new UserActionRequiredException(webappMessage);
		}
		
		return new CmisBrowsingURLConnection(cuc, serverUrl);
	}

}
