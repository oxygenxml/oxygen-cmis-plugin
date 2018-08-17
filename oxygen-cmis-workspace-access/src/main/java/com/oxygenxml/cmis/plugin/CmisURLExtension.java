package com.oxygenxml.cmis.plugin;

import java.net.URLStreamHandler;
import java.util.logging.Logger;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerPluginExtension;

public class CmisURLExtension implements URLStreamHandlerPluginExtension {

	private static final Logger logger = Logger.getLogger(CmisURLExtension.class.getName());
	public static final String CMIS_PROTOCOL = "cmis";

	/**
	 * 
	 */
	@Override
	public URLStreamHandler getURLStreamHandler(String protocol) {
		logger.info("CmisURLExtension.getURLStreamHandler() ---> " + protocol);
		if (protocol.startsWith(CmisURLConnection.CMIS_PROTOCOL)) {
			return new CmisStreamHandler();
		}
		return null;
	}

}