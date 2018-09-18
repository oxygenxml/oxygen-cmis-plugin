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
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.basic.util.URLUtil;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;

public class CmisURLConnection extends URLConnection {
	/**
	 * Logging support.
	 */
	private static Logger logger = Logger.getLogger(CmisURLConnection.class);

	private CMISAccess cmisAccess;
	private ResourceController resourceController;
	private UserCredentials credentials;

	// KEYWORDS
	public static final String CMIS_PROTOCOL = "cmis";
	private static final String REPOSITORY_PARAM = "repo";
	private static final String PATH_PARAM = "path";
	private static final String CONTENT_SAMPLE = "Empty";
	private static final String DOC_TYPE = "VersionableType";

	// CONSTRUCTOR
	public CmisURLConnection(URL url, CMISAccess cmisAccess, UserCredentials credentials) {
		super(url);
		this.cmisAccess = cmisAccess;
		this.credentials = credentials;
	}

	/**
	 * 
	 * @param object
	 * @param _ctrl
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String generateURLObject(CmisObject object, ResourceController ctrl, String parentPath) {
		StringBuilder urlb = new StringBuilder();

		// Get and encode server URL
		String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);
		String repository = ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID);
		originalProtocol = URLUtil.encodeURIComponent(originalProtocol);

		// Generate first part of custom URL
		urlb.append((CMIS_PROTOCOL + "://")).append(originalProtocol).append("/").append(repository);

		Boolean invalidPath = true;
		// Get path of Cmis Object
		List<String> objectPath = ((FileableCmisObject) object).getPaths();

		parentPath = URLUtil.decodeURIComponent(parentPath);
		if (parentPath.contains(repository)) {
			parentPath = parentPath.replace(repository + "/", "");
		}

		// Append object path to URL
		for (int i = 0; i < objectPath.size(); i++) {
			// Check if path(i) start with path of parent folder
			if (objectPath.get(i).startsWith(parentPath)) {
				invalidPath = false;
				for (String pth : objectPath.get(i).split("/")) {
					if (!pth.isEmpty()) {
						urlb.append("/").append(URLUtil.encodeURIComponent(pth));
					}
				}
				break;
			}
		}

		if (invalidPath) {
			urlb.append("/").append(parentPath).append(object.getName());
		}

		return urlb.toString();
	}

	/**
	 * Overload the one with parent path.
	 * 
	 * @param object
	 * @param ctrl
	 * @return
	 */
	public static String generateURLObject(CmisObject object, ResourceController ctrl) {
		StringBuilder urlb = new StringBuilder();

		logger.info("Generate URL for: " + object.getName());

		// Get and encode server URL
		String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);
		String repository = ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID);
		originalProtocol = URLUtil.encodeURIComponent(originalProtocol);

		// Generate first part of custom URL
		urlb.append((CMIS_PROTOCOL + "://")).append(originalProtocol).append("/").append(repository);

		// Get and apend to URL path of Cmis Object
		List<String> objectPath = ((FileableCmisObject) object).getPaths();
		logger.info("Paths " + objectPath);
		for (int i = 0; i < objectPath.size(); i++) {
			for (String pth : objectPath.get(i).split("/")) {
				if (!pth.isEmpty()) {
					urlb.append("/").append(URLUtil.encodeURIComponent(pth));
				}
			}
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
	 * @throws UserActionRequiredException
	 */
	public CmisObject getCMISObject(String url)
			throws CmisUnauthorizedException, CmisObjectNotFoundException, MalformedURLException {
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
		resourceController = cmisAccess.createResourceController();

		// Get the object path
		String path = param.get(PATH_PARAM);

		// Get and return from server cmis object
		CmisObject objectFromURL = resourceController.getSession().getObjectByPath(path);

		return objectFromURL;
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
	public static URL getServerURL(String customURL, Map<String, String> param) throws MalformedURLException {
		// Replace CMIS part
		if (customURL.startsWith(CmisURLConnection.CMIS_PROTOCOL)) {
			customURL = customURL.replaceFirst((CMIS_PROTOCOL + "://"), "");
		}

		String originalProtocol = customURL;
		// Get server URL, put it into originalProtocol and replace from customURL
		originalProtocol = originalProtocol.substring(0, originalProtocol.indexOf("/"));
		customURL = customURL.replaceFirst(originalProtocol, "");
		customURL = customURL.replaceFirst("/", "");

		// Save Repository and object path in HashMap
		if (param != null) {
			param.put(REPOSITORY_PARAM, customURL.substring(0, customURL.indexOf("/")));
			customURL = customURL.replaceFirst(param.get(REPOSITORY_PARAM), "");
			customURL = URLUtil.decodeURIComponent(customURL);
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
	}

	@Override
	public InputStream getInputStream() throws IOException {
		Document document = (Document) getCMISObject(getURL().toExternalForm());
		Document PWC = null;
		
		if (document.isVersionSeriesCheckedOut()) {
			String pwc = document.getVersionSeriesCheckedOutId();
			PWC = (Document) resourceController.getSession().getObject(pwc);
		} else {
			return document.getContentStream().getStream();
		}

		return PWC.getContentStream().getStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				Document document = null;
				String documentUrl = null;
				boolean newDocument = false;

				try {
					documentUrl = getURL().toExternalForm();
					document = (Document) getCMISObject(documentUrl);

				} catch (CmisObjectNotFoundException e) {
					// If created document doesn't exist we create one
					documentUrl = createDocument();
					document = (Document) getCMISObject(documentUrl);
					newDocument = true;
				}

				// All bytes have been written.
				byte[] byteArray = toByteArray();

				ContentStreamImpl contentStream = new ContentStreamImpl(document.getName(),
						BigInteger.valueOf(byteArray.length), document.getContentStreamMimeType(),
						new ByteArrayInputStream(byteArray));

				/**
				 * Here we check if document is versionable and do operation. For versionable
				 * document we get the PWC to save the new content stream. After this we check
				 * in (as minor) document if it wasn't check out. If document is newly we
				 * check-in it as major version and saving the template content stream from
				 * client-side.
				 * 
				 */
				if (!document.isVersionable()) {
					document.setContentStream(contentStream, true);
				} else {
					Document PWC = null;
					boolean wasChecked = false;
					document = document.getObjectOfLatestVersion(false);

					if (document.isVersionSeriesCheckedOut()) {
						String pwc = document.getVersionSeriesCheckedOutId();
						PWC = (Document) resourceController.getSession().getObject(pwc);
					} else {
						PWC = (Document) resourceController.getSession().getObject(document.checkOut());
						wasChecked = true;
					}

					PWC.setContentStream(contentStream, true);

					if (newDocument) {
						PWC.checkIn(true, null, null, " ");
					} else if (wasChecked) {
						PWC.checkIn(false, null, null, " ");
					}
				}
			}
		};
	}

	/**
	 * Create new document as versionable and generate URL if doesn't exist.
	 * 
	 * @param byteArray
	 * @param typeOfDocument
	 * 
	 * @param document
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String createDocument() throws MalformedURLException, UnsupportedEncodingException {
		HashMap<String, String> param = new HashMap<>();
		getServerURL(url.toExternalForm(), param);

		String path = param.get(PATH_PARAM);
		String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());

		path = path.replace(fileName, "");

		String mimeType = MimeTypes.getMIMEType(fileName.substring(fileName.indexOf("."), fileName.length()));
		if (mimeType == "application/octet-stream") {
			mimeType = "text/xml";
		}

		Folder rootFolder = (Folder) cmisAccess.getSession().getObjectByPath(path);
		Document document = resourceController.createVersionedDocument(rootFolder, fileName, CONTENT_SAMPLE, mimeType,
				DOC_TYPE, VersioningState.MINOR);

		return generateURLObject(document, resourceController, path);
	}

	/**
	 * 
	 * @param connectionUrl
	 * @return ResourceController
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws UserActionRequiredException
	 */
	public ResourceController getResourceController(String connectionUrl) throws MalformedURLException {
		getCMISObject(connectionUrl);
		return resourceController;
	}

	/**
	 * 
	 * @return CMISAccess
	 */
	public CMISAccess getCMISAccess() {
		return cmisAccess;
	}

	/**
	 * 
	 * @return UserCredentials
	 */
	public UserCredentials getUserCredentials() {
		return credentials;
	}
}
