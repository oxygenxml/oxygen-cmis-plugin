package com.oxygenxml.cmis.plugin;

import java.net.URLStreamHandler;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CmisURL;

import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerPluginExtension;

/**
 * Extension point to handle CMIS protocol. 
 */
public class CmisURLExtension implements URLStreamHandlerPluginExtension {
  /**
   * Logger for logging.
   */
	private static final Logger logger = Logger.getLogger(CmisURLExtension.class.getName());
	
	@Override
	public URLStreamHandler getURLStreamHandler(String protocol) {
	  if (logger.isDebugEnabled()) {
      logger.debug("Get url stream handler for protocol "+ protocol);
    }
	  
		if (protocol.startsWith(CmisURL.CMIS_PROTOCOL)) {
			return new CmisStreamHandler();
		}
		
		return null;
	}
}