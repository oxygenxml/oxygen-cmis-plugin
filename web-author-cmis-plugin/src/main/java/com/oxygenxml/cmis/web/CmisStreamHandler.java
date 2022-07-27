package com.oxygenxml.cmis.web;


import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.CmisURL;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import lombok.extern.slf4j.Slf4j;
import ro.sync.basic.util.URLUtil;
import ro.sync.ecss.extensions.api.webapp.WebappMessage;
import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContext;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;

@Slf4j
public class CmisStreamHandler extends URLStreamHandlerWithContext {


	@Override
	protected URLConnection openConnectionInContext(String contextId, URL url, Proxy proxy) throws IOException {
	  // WA-5824: The URL should not contain anchor anyway, but we strip it so that some users can 
	  //   use the updated plugin before the fix landed in WA.
	  url = URLUtil.removeAnchor(url);
	  
		// Getting credentials and another information
		UserCredentials credentials = CredentialsManager.INSTANCE.getCredentials(contextId);
		CMISAccess cmisAccess = new CMISAccess();
		CmisURLConnection cuc = new CmisURLConnection(url, cmisAccess, credentials);
		URL serverUrl = CmisURL.parseServerUrl(url.toExternalForm());

		log.info("Server URL: " + serverUrl.toExternalForm());

		boolean isUserValid = true;
		if (credentials != null && !credentials.isEmpty()) {
			try {
				cmisAccess.pureConnectToServer(serverUrl, credentials);
			} catch (Exception e) {
			  // may be CmisUnauthorizedException
			  log.error(e, e);
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
