package com.oxygenxml.cmis.core.urlhandler;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;

import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContext;

public class CmisStreamHandler extends URLStreamHandlerWithContext {
	
	private static final Logger logger = Logger.getLogger(CmisStreamHandler.class.getName());
	
	@Override
	protected URLConnection openConnectionInContext(String contextId, URL url, Proxy proxy) throws IOException {
		
		logger.info("CmisStreamHandler URL --> " + url.toExternalForm());

		CmisURLConnection cuc = new CmisURLConnection(url, CMISAccess.getInstance());
		
		return new CmisBrowsingURLConnection(cuc);

	}
}
