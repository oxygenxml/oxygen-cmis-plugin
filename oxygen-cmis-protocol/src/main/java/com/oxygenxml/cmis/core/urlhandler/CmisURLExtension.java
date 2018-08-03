package com.oxygenxml.cmis.core.urlhandler;

import java.io.UnsupportedEncodingException;
import java.net.URLStreamHandler;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.ResourceController;

import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerPluginExtension;

public class CmisURLExtension  implements URLStreamHandlerPluginExtension {

	private static final Logger logger = Logger.getLogger(CmisURLExtension.class.getName());
	/**
	 * 
	 * cmis://escaped_host_url/repoID/path
	 * 
	 * @param object
	 * @param ctrl
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getCustomURL(CmisObject object, ResourceController ctrl) throws UnsupportedEncodingException {
		if (object == null || ctrl == null) {
			throw new NullPointerException();
		}
		return CmisURLConnection.generateURLObject(object, ctrl);
	}

	/**
	 * 
	 */
	@Override
	public URLStreamHandler getURLStreamHandler(String protocol) {
		logger.info("CmisURLExts getURLStrHndl --> " + protocol);
		if (protocol.startsWith(CmisURLConnection.CMIS_PROTOCOL)) {			
			return new CmisStreamHandler();
		}
		return null;
	}
}
