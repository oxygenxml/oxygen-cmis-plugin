package com.oxygenxml.cmis.web;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContext;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CmisStreamHandler extends URLStreamHandlerWithContext {

	private static final Logger logger = Logger.getLogger(CmisStreamHandler.class.getName());

	@Override
	protected URLConnection openConnectionInContext(String contextId, URL url, Proxy proxy) throws IOException {
		// Accessing webapp to get credentials
		WebappPluginWorkspace workspace = (WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace();
		SessionStore sessionStore = workspace.getSessionStore();

		// Getting credentials
		UserCredentials credentials = sessionStore.get(contextId, "credentials");

		logger.info("CmisStreamHandler.openConnectionInContext() ---> " + url.toExternalForm() + " -- "
				+ credentials.toString());

		// Creating URLConnection with credentials
		CmisURLConnection cuc = new CmisURLConnection(url, new CMISAccess());

		return new CmisBrowsingURLConnection(cuc, credentials);
	}

}
