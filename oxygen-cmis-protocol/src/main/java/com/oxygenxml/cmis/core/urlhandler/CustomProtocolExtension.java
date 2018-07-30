package com.oxygenxml.cmis.core.urlhandler;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLStreamHandler;

import org.apache.chemistry.opencmis.client.api.CmisObject;

import com.oxygenxml.cmis.core.ResourceController;

import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerPluginExtension;

public class CustomProtocolExtension implements URLStreamHandlerPluginExtension {

	/**
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
		
		return CustomProtocol.generateURLObject(object, ctrl);
	}

	/**
	 * Helper method to get CmisObject from URL
	 * 
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException 
	 */
	public CmisObject getObjectFromURL(String url) throws MalformedURLException, UnsupportedEncodingException {
		if (url == null) {
			throw new NullPointerException();
		}
		return new CustomProtocol().getCMISObject(url);
	}

	/**
	 * Helper methods to get content from cmis:document using URL
	 * 
	 * @param url
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	public Reader getContentURL(String url, ResourceController ctrl)
			throws UnsupportedEncodingException, MalformedURLException {
		if (url == null) {
			throw new NullPointerException();
		}
		return new CustomProtocol().getDocumentContent(url, ctrl);
	}

	/**
	 * 
	 */
	@Override
	public URLStreamHandler getURLStreamHandler(String protocol) {
		if (CustomProtocol.CMIS_PROTOCOL.equals(protocol)) {
			return new CustomProtocol();
		}

		return null;
	}
}
