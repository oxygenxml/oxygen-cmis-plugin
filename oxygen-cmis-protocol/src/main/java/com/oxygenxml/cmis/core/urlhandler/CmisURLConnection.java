package com.oxygenxml.cmis.core.urlhandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.basic.util.URLUtil;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;

// Auth - URLStreamHandlerWithContext
// File Browsing
// Checkin / Checkout

public class CmisURLConnection extends URLConnection {

	private static final Logger logger = Logger.getLogger(CmisURLConnection.class.getName());

	private CMISAccess cmisAccess;
	private ResourceController bigCtrl;
	private UserCredentials credentials;

	// KEYWORDS
	public static final String CMIS_PROTOCOL = "cmis";
	private static final String REPOSITORY_PARAM = "repo";
	private static final String PATH_PARAM = "path";

	// CONSTRUCTOR
	public CmisURLConnection(URL url, CMISAccess cmisAccess) {
		super(url);
		this.cmisAccess = cmisAccess;
	}
	
	/**
	 * 
	 * @param credentials
	 */
	public void setCredentials(UserCredentials credentials) {
		this.credentials = credentials;
		//cmisAccess.connectToRepo(url, null, credentials);
	}
	
	/**
	 * 
	 * @param object
	 * @param _ctrl
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	// TODO: param parentURL
	public static String generateURLObject(CmisObject object, ResourceController _ctrl)
			 {
		ResourceController ctrl = _ctrl;

		// Builder for building custom URL
		StringBuilder urlb = new StringBuilder();

		// Get server URL
		String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);

		// Encode server URL
		originalProtocol = URLUtil.encodeURIComponent(originalProtocol);

		// Generate first part of custom URL
		urlb.append((CMIS_PROTOCOL + "://")).append(originalProtocol).append("/");
		urlb.append(ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID));

		// Get path of Cmis Object
		List<String> objectPath = ((FileableCmisObject) object).getPaths();

		// TODO: if (!parentURL.isEmpty()) {
		// !!!!!!!!!!!!
		// Appeding file path to URL and encode spaces
		for (int i = 0; i < objectPath.size(); i++) {
			// TODO: if (objectPath.startsWith(parentURL)) {
			for (String pth : objectPath.get(i).split("/")) {
				if (!pth.isEmpty()) {
					urlb.append("/").append(URLUtil.encodeURIComponent(pth));
				}
			}
		}
		// } else { /* The file should be in the root folder - there is no path to
		// append. Only the file name*/ }
		return urlb.toString();
	}

	/**
	 * Gets the CmisObject identified by the given URL.
	 * 
	 * @param url
	 *            URL identifying a CMIS resource.
	 * 
	 * @return The CMIS object identified by the custom URL.
	 * 
	 * @throws MalformedURLException
	 *             If the URL doesn't contain the expected syntax.
	 * @throws UnsupportedEncodingException
	 * @throws UserActionRequiredException
	 */
	public CmisObject getCMISObject(String url) throws MalformedURLException, UnsupportedEncodingException,
			CmisUnauthorizedException, UserActionRequiredException {

		// Decompose the custom URL in query elements
		Map<String, String> param = new HashMap<>();

		// Get from custom URL server URL for connection
		URL serverURL = getServerURL(url, param);

		// Get repository ID from custom URL for connection
		String repoID = param.get(REPOSITORY_PARAM);
		if (repoID == null) {
			throw new MalformedURLException("Mising repository ID inside: " + url);
		}

		// Accessing the server using params which we gets
		cmisAccess.connectToRepo(serverURL, repoID, credentials);
		bigCtrl = cmisAccess.createResourceController();

		// Get the object path
		String path = param.get(PATH_PARAM);

		// Get and return from server cmis object
		return bigCtrl.getSession().getObjectByPath(path);
	}

	/**
	 * Builder server URL form given custom URL.
	 * 
	 * @param customURL
	 * @param queryParams
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public URL getServerURL(String customURL, Map<String, String> param)
			throws MalformedURLException, UnsupportedEncodingException {

		logger.info("CmisURLConnection.getServerURL() --> " + customURL);

		// Replace CMIS part
		if (customURL.startsWith(CmisURLConnection.CMIS_PROTOCOL)) {
			customURL = customURL.replaceFirst((CMIS_PROTOCOL + "://"), "");
		} else {
			// Test only!
			customURL = customURL.replaceFirst(customURL.substring(0, customURL.indexOf("://") + "://".length()), "");
		}

		String originalProtocol = customURL;

		// Get server URL, put it into originalProtocol and replace from customURL
		originalProtocol = originalProtocol.substring(0, originalProtocol.indexOf("/"));
		customURL = customURL.replaceFirst(originalProtocol, "");

		customURL = customURL.replaceFirst("/", "");

		// Save Repository
		if (param != null) {
			param.put(REPOSITORY_PARAM, customURL.substring(0, customURL.indexOf("/")));
		}

		if (param != null) {
			customURL = customURL.replaceFirst(param.get(REPOSITORY_PARAM), "");
		}
		customURL = URLUtil.decodeURIComponent(customURL);

		// Save file path
		if (param != null) {
			param.put(PATH_PARAM, customURL);
		}

		// Creating server URL
		originalProtocol = URLUtil.decodeURIComponent(originalProtocol);
		String protocol = originalProtocol.substring(0, originalProtocol.indexOf("://"));

		URL url = new URL(originalProtocol + "/");

		URL serverURL = new URL(protocol, url.getHost(), url.getPort(),
				url.getPath().substring(0, url.getPath().lastIndexOf("/")));

		return serverURL;
	}

	@Override
	public void connect() throws IOException {
		// Not sure if we should do something.
		// super.connect();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		Document document = (Document) getCMISObject(getURL().toExternalForm());

		return document.getContentStream().getStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				// All bytes have been written.
				Document document = (Document) getCMISObject(getURL().toExternalForm());
				byte[] byteArray = toByteArray();
				ContentStream contentStream = new ContentStreamImpl(document.getName(),
						BigInteger.valueOf(byteArray.length), document.getContentStreamMimeType(),
						new ByteArrayInputStream(byteArray));

				// TODO What to do if the system created a new document.
				// TODO Maybe refresh the browser....
				document.setContentStream(contentStream, true);
			}
		};
	}

	/**
	 * 
	 * @param url1
	 * @return ResourceController
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws UserActionRequiredException
	 */
	public ResourceController getCtrl(URL url1)
			throws MalformedURLException, UnsupportedEncodingException, UserActionRequiredException {
		getCMISObject(url1.toExternalForm());
		return bigCtrl;
	}

	/**
	 * 
	 * @param url1
	 * @return Repository List
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<Repository> getReposList(URL url1, UserCredentials credentials)
			throws MalformedURLException, UnsupportedEncodingException, CmisUnauthorizedException {

		String serverUrl = url1.toExternalForm();

		if (serverUrl.startsWith(CmisURLConnection.CMIS_PROTOCOL)) {
			serverUrl = serverUrl.replaceFirst((CMIS_PROTOCOL + "://"), "");
		} else {
			serverUrl = serverUrl.replaceFirst(serverUrl.substring(0, serverUrl.indexOf("://") + "://".length()), "");
		}

		serverUrl = URLUtil.decodeURIComponent(serverUrl);
		List<Repository> list = cmisAccess.connectToServerGetRepositories(new URL(serverUrl), credentials);
		
		return list;
	}

	public CMISAccess getAccess() {
		return cmisAccess;
	}
}
