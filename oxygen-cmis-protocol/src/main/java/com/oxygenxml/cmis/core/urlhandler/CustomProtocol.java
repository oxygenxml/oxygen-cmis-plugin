package com.oxygenxml.cmis.core.urlhandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;
import ro.sync.net.protocol.FileBrowsingConnection;
import ro.sync.net.protocol.FolderEntryDescriptor;

// Auth - URLStreamHandlerWithContext
// File Browsing
// Checkin / Checkout

/**
 * Handles the "cmis" protocol used to identify CMIS resources.
 * 
 *
 */
public class CustomProtocol extends URLStreamHandler implements FileBrowsingConnection {

	Logger logger = Logger.getLogger(CustomProtocol.class.getName());
	// KEYWORDS
	public static final String CMIS_PROTOCOL = "cmis";
	private static final String REPOSITORY_PARAM = "repo";
	private static final String OBJECT_ID_PARAM = "objID";
	private static final String PATH_PARAM = "path";
	private static final String SERVER_KEY = "key";

	private Document updatedDocument = null;

	/**
	 * 
	 * TODO Code review. When building and extracting, we should handle this case as
	 * well. THis is the AtomPub URL for Sharepoint:
	 * 
	 * http://<host>/_vti_bin/cmis/rest?getRepositories
	 * 
	 * It already has something inside GET. We must be able to recreate the URL as
	 * it was.
	 * 
	 * @param object
	 * @param _ctrl
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static String generateURLObject(CmisObject object, ResourceController _ctrl) throws UnsupportedEncodingException {
		ResourceController ctrl = _ctrl;

		// Builder for building custom URL
		StringBuilder urlb = new StringBuilder();

		// Get server URL
		String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);
		/*// Store protocol (http://)
		String protocol = originalProtocol.substring(0, originalProtocol.indexOf("://"));

		String key = originalProtocol.substring(originalProtocol.lastIndexOf("/"), originalProtocol.length())
				.concat("/");*/

		// Replace the protocol with CMIS_PROTOCOL
		
		
		originalProtocol = URLEncoder.encode(originalProtocol, "UTF-8");
		
		urlb.append((CMIS_PROTOCOL + "://"))
			.append(originalProtocol).append("/");
			/*.append("/" + SERVER_KEY + "!").append(originalProtocol.length()).append("/");*/
		
		urlb.append(ctrl.getSession()
				.getSessionParameters()
				.get(SessionParameter.REPOSITORY_ID));


		// Get path of Cmis Object
		List<String> objectPath = ((FileableCmisObject) object).getPaths();

		// Generate URL (append to urlb) and generate query for CMISOBJECT_PATH
		for (String pth : objectPath) {
			urlb.append(pth);
		}

		/*path.replace(0, 1, "");*/

		// urlb.append(((FileableCmisObject) object).getPaths());
/*
		urlb.append("?" + REPOSITORY_PARAM + "=")
				.append(ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID));

		//urlb.append("&" + OBJECT_ID_PARAM + "=").append(object.getId());
		urlb.append("&" + PROTOCOL_PARAM + "=").append(protocol);
		// urlb.append("&" + CMISOBJECT_PATH + "=").append(path.toString());
		urlb.append("&" + "key" + "=").append(key);
*/
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

		System.out.println(serverURL);
		
		// Get repository ID from custom URL for connection
		String repoID = param.get(REPOSITORY_PARAM);
		if (repoID == null) {
			throw new MalformedURLException("Mising repository ID inside: " + url);
		}

		// Accessing the server using params which we gets
		CMISAccess.getInstance().connect(serverURL, repoID);
		ResourceController ctrl = CMISAccess.getInstance().createResourceController();
		
		// Get the object path
		String path = param.get(PATH_PARAM);

		// Get and return from server cmis object
		return ctrl.getSession().getObjectByPath(path);
	}

	/**
	 * Using URL get cmis:document from server and return its content
	 * 
	 * @param url
	 * @return InputReader of cmis:document
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	public Reader getDocumentContent(String url, ResourceController ctrl)
			throws UnsupportedEncodingException, MalformedURLException {

		// Decompose the custom URL in query elements
		Map<String, String> params = getQueryParams(url);

		// Get object ID

		String objectID = params.get(OBJECT_ID_PARAM);
		if (objectID == null) {
			throw new MalformedURLException("Mising object ID inside: " + url);
		}

		// Call getDocumentContent method to get cmis document content
		return ctrl.getDocumentContent(objectID);
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
	public URL getServerURL(String customURL, Map<String, String> param) throws MalformedURLException, UnsupportedEncodingException {
		// Get protocol from query
		/*String protocol = queryParams.get(PROTOCOL_PARAM);
		String key = queryParams.get(SERVER_KEY);
		customURL = customURL.replaceFirst(customURL.substring(0, customURL.indexOf("://")), protocol);
		customURL = customURL.substring(0, customURL.indexOf(key) + key.length());*/

		
	/*	String lengths = customURL
				.substring(customURL.indexOf(SERVER_KEY) 
				+ (SERVER_KEY).length(), customURL.indexOf("!"));*/
		
/*		customURL = customURL.replaceFirst(lengths, "");
		int length = Integer.parseInt(lengths);*/
		
		logger.info("start  " + customURL);
		
		String originalProtocol = customURL.replaceFirst((CMIS_PROTOCOL + "://"), "");
			//   originalProtocol = originalProtocol.substring(0, length);
		originalProtocol = originalProtocol.substring(0, originalProtocol.indexOf("/"));
		
		
		customURL = customURL.replaceFirst(originalProtocol, "");
		customURL = customURL.replaceFirst((CMIS_PROTOCOL + "://"), "");
		customURL = customURL.replaceFirst("/", "");
	//	customURL = customURL.replaceFirst(("/" + SERVER_KEY + lengths + "/") , "");
		
		param.put(REPOSITORY_PARAM, customURL.substring(0, customURL.indexOf("/")));
		
		logger.info("here");
		customURL = customURL.replaceFirst(param.get(REPOSITORY_PARAM), "");
		
		param.put(PATH_PARAM, customURL);
		
		originalProtocol = URLDecoder.decode(originalProtocol, "UTF-8");
		String protocol = originalProtocol.substring(0, originalProtocol.indexOf("://"));
		
		URL url = new URL(originalProtocol + "/");

		URL serverURL = new URL(protocol, url.getHost(), url.getPort(),
				url.getPath().substring(0, url.getPath().lastIndexOf("/")));

		// TODO Put back the query part that isn't ours....

		return serverURL;
	}

	/**
	 * MAP of query parameters inside given URL.
	 * 
	 * @param customURL
	 * @return
	 */
	private Map<String, String> getQueryParams(String customURL) {
		Map<String, String> params = new HashMap<>();

		String queryPart = customURL.substring(customURL.indexOf("?") + 1, customURL.length());
		String[] pairs = queryPart.split("&");

		for (int i = 0; i < pairs.length; i++) {
			String[] nameVal = pairs[i].split("=");
			params.put(nameVal[0], nameVal.length > 1 ? nameVal[1] : null);
		}

		return params;
	}

/*	private String getPathFromURL(String url, Map<String, String> params) {
		String key = params.get(SERVER_KEY);
		String path = "";
		
		if (url.indexOf("?") != -1) {
			path = url.substring(url.indexOf(key) + key.length() - 1, url.indexOf("?"));
		} else {
			path = url.substring(url.indexOf(key) + key.length() - 1, url.indexOf("&"));;
		}
		
		return path;
	}
*/	
	
	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		return new CMISURLConnection(u);
	}

	/**
	 * Connection to a CMIS Server.
	 */
	private class CMISURLConnection extends URLConnection {

		protected CMISURLConnection(URL url) {
			super(url);
		}

		@Override
		public void connect() throws IOException {
			// Not sure if we should do something.
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
					updatedDocument = document.setContentStream(contentStream, true);
				}
			};
		}

	}

	/**
	 * Method to take up
	 * 
	 * @return
	 * @throws Exception
	 */
	public Document getUpdatedDocument() throws Exception {
		if (updatedDocument == null) {
			throw new Exception();
		}
		return updatedDocument;
	}

	@Override
	public List<FolderEntryDescriptor> listFolder() throws IOException, UserActionRequiredException {
		// TODO: file browsing

		return null;
	}

}
