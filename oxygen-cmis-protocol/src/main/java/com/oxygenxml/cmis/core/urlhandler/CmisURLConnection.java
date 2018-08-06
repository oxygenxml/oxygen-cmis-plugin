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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;

// Auth - URLStreamHandlerWithContext
// File Browsing
// Checkin / Checkout

public class CmisURLConnection extends URLConnection {

	private static final Logger logger = Logger.getLogger(CmisURLConnection.class.getName());
	private CMISAccess cmisAccess;
	private ResourceController bigCtrl;

	public CmisURLConnection(URL url, CMISAccess cmisAccess) {
		super(url);
		this.cmisAccess = cmisAccess;
	}

	// KEYWORDS
	public static final String CMIS_PROTOCOL = "cmis";
	private static final String REPOSITORY_PARAM = "repo";
	private static final String PATH_PARAM = "path";

	/**
	 * It already has something inside GET. We must be able to recreate the URL as
	 * it was.
	 * 
	 * @param object
	 * @param _ctrl
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String generateURLObject(CmisObject object, ResourceController _ctrl)
			throws UnsupportedEncodingException {
		ResourceController ctrl = _ctrl;

		// Builder for building custom URL
		StringBuilder urlb = new StringBuilder();

		// Get server URL
		String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);

		originalProtocol = URLEncoder.encode(originalProtocol, "UTF-8");

		urlb.append((CMIS_PROTOCOL + "://")).append(originalProtocol).append("/");
		urlb.append(ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID));

		// Get path of Cmis Object
		List<String> objectPath = ((FileableCmisObject) object).getPaths();

		// Generate URL (append to urlb) and generate query for CMISOBJECT_PATH
		for (String pth : objectPath) {
			urlb.append(pth);
		}

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
	 */
	public CmisObject getCMISObject(String url) throws MalformedURLException, UnsupportedEncodingException {
		// TODO Code review: Let's extract some constants. PROTOCOL, REPOSITORY

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
		cmisAccess.connectToRepo(serverURL, repoID);
		ResourceController ctrl = cmisAccess.createResourceController();

		// Get the object path
		String path = param.get(PATH_PARAM);

		// Get and return from server cmis object
		return ctrl.getSession().getObjectByPath(path);
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

		logger.info("getServerURL --> " + customURL);

		String originalProtocol = "";

		if (customURL.startsWith(CmisURLConnection.CMIS_PROTOCOL)) {
			originalProtocol = customURL.replaceFirst((CMIS_PROTOCOL + "://"), "");

		// ONLY FOR TEST!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		} else { originalProtocol = customURL.replaceFirst(("https://"), ""); }
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

		originalProtocol = originalProtocol.substring(0, originalProtocol.indexOf("/"));

		customURL = customURL.replaceFirst(originalProtocol, "");

		if (customURL.startsWith(CmisURLConnection.CMIS_PROTOCOL)) {
			customURL = customURL.replaceFirst((CMIS_PROTOCOL + "://"), "");

		// ONLY FOR TEST!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		} else { customURL = customURL.replaceFirst(("https://"), ""); }
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

		customURL = customURL.replaceFirst("/", "");

		if (param != null) {
			param.put(REPOSITORY_PARAM, customURL.substring(0, customURL.indexOf("/")));
		}

		logger.info("here");
		customURL = customURL.replaceFirst(param.get(REPOSITORY_PARAM), "");

		if (param != null) {
			param.put(PATH_PARAM, customURL);
		}

		originalProtocol = URLDecoder.decode(originalProtocol, "UTF-8");
		String protocol = originalProtocol.substring(0, originalProtocol.indexOf("://"));

		URL url = new URL(originalProtocol + "/");

		URL serverURL = new URL(protocol, url.getHost(), url.getPort(),
				url.getPath().substring(0, url.getPath().lastIndexOf("/")));

		// TODO Put back the query part that isn't ours....
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



	public ResourceController getCtrl(URL url1) throws MalformedURLException, UnsupportedEncodingException {
		// TODO Auto-generated method stub
		Map<String, String> param = new HashMap<>();
		URL serverUrl = getServerURL(url1.toExternalForm(), param);

		String repoID = param.get(REPOSITORY_PARAM);
		if (repoID == null) {
			throw new MalformedURLException("Mising repository ID inside: " + url1);
		}

		// Accessing the server using params which we gets
		cmisAccess.connectToRepo(serverUrl, repoID);

		bigCtrl = cmisAccess.createResourceController();
		
		return bigCtrl;
	}
}
