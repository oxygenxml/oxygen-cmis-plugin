package com.oxygenxml.cmis.web;

import java.net.URLStreamHandler;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CmisURL;

import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerPluginExtension;

public class CmisURLExtension implements URLStreamHandlerPluginExtension {

	private static final Logger logger = Logger.getLogger(CmisURLExtension.class.getName());

	/**
	 * 
	 */
	@Override
	public URLStreamHandler getURLStreamHandler(String protocol) {
		logger.info("CmisURLExtension.getURLStreamHandler() ---> " + protocol);
		if (protocol.startsWith(CmisURL.CMIS_PROTOCOL)) {
			return new CmisStreamHandler();
		}
		return null;
	}
}
