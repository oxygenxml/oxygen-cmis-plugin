package com.oxygenxml.cmis.web;

import java.net.URLStreamHandler;

import com.oxygenxml.cmis.core.CmisURL;

import lombok.extern.slf4j.Slf4j;
import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerPluginExtension;

@Slf4j
public class CmisURLExtension implements URLStreamHandlerPluginExtension {

  @Override
	public URLStreamHandler getURLStreamHandler(String protocol) {
		log.info("CmisURLExtension.getURLStreamHandler() ---> " + protocol);
		if (protocol.startsWith(CmisURL.CMIS_PROTOCOL)) {
			return new CmisStreamHandler();
		}
		return null;
	}
}
