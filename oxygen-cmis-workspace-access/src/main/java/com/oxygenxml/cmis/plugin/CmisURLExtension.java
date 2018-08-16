package com.oxygenxml.cmis.plugin;

import java.net.URLStreamHandler;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.basic.util.URLUtil;
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

	public static String generateURLObject(CmisObject object, ResourceController ctrl) {
		// Builder for building custom URL
		StringBuilder urlb = new StringBuilder();

		// Get server URL
		String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);
		String repository = ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID);
		// Encode server URL
		originalProtocol = URLUtil.encodeURIComponent(originalProtocol);

		// Generate first part of custom URL
		urlb.append((CMIS_PROTOCOL + "://")).append(originalProtocol).append("/").append(repository);

		// Get path of Cmis Object
		List<String> objectPath = ((FileableCmisObject) object).getPaths();

		// Append object path to URL
		for (int i = 0; i < objectPath.size(); i++) {

			logger.info("here");
			for (String pth : objectPath.get(i).split("/")) {
				if (!pth.isEmpty()) {
					urlb.append("/").append(URLUtil.encodeURIComponent(pth));
				}
			}
		}
		return urlb.toString();
	}

}
