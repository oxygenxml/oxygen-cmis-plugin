package com.oxygenxml.cmis.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.action.CmisAction;

import ro.sync.ecss.extensions.api.webapp.WebappMessage;
import ro.sync.ecss.extensions.api.webapp.plugin.FilterURLConnection;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;
import ro.sync.net.protocol.FolderEntryDescriptor;

public class CmisBrowsingURLConnection extends FilterURLConnection {
	private static final Logger logger = Logger.getLogger(CmisBrowsingURLConnection.class.getName());

	private static final String ERROR_MESSAGE = "Invalid username or password!";
	private static final String ERROR_CODE = "401";
	
	private CmisURLConnection connection;
	private URL serverUrl;

	
	public CmisBrowsingURLConnection(URLConnection delegateConnection, URL serverUrl) {
		super(delegateConnection);
		this.connection = (CmisURLConnection) delegateConnection;
		this.serverUrl = serverUrl;
	}

	
	/**
	 * Get the InputStream of document.
	 * If document is an old version, get this document
	 * using Id which is stored in query part of URL.
	 * 
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		try {
			if (this.url.getQuery() != null) {
				if(this.url.getQuery().contains(CmisAction.OLD_VERSION.getValue())) {
					return getOlderVersionInputStream();
				}
			}

			return super.getInputStream();

		} catch (CmisUnauthorizedException e) {
			WebappMessage webappMessage = new WebappMessage(WebappMessage
					.MESSAGE_TYPE_ERROR, ERROR_CODE, ERROR_MESSAGE, true);
			
			throw new UserActionRequiredException(webappMessage);
		}
	}

	
	/**
	 * If document is an older version, we get InputStream
	 * of this document selected by id.
	 * 
	 * @return InputStream of older version document.
	 * @throws MalformedURLException
	 */
	private InputStream getOlderVersionInputStream() throws MalformedURLException {
		HashMap<String, String> queryPart = new HashMap<>();

		for (String pair : url.getQuery().split("&")) {
			int index = pair.indexOf('=');
			queryPart.put(pair.substring(0, index), pair.substring(index + 1));
		}

		String objectId = queryPart.get(CmisAction.OLD_VERSION.getValue());
		String connectionUrl = this.url.toExternalForm()
				.replace(this.url.getQuery(), "");

		connectionUrl = connectionUrl.replace("?", "");
		Document document = (Document) connection
				.getResourceController(connectionUrl).getCmisObj(objectId);

		return document.getContentStream().getStream();
	}
	
	
	/**
	 * Get OutputStream of document.
	 * 
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		try {
			return super.getOutputStream();
		} catch (CmisUnauthorizedException e) {
			WebappMessage webappMessage = new WebappMessage(WebappMessage
					.MESSAGE_TYPE_ERROR, ERROR_CODE, ERROR_MESSAGE, true);
			
			throw new UserActionRequiredException(webappMessage);
		}
	}

	
	/**
	 * Generate FolderEntryDescriptor for any object
	 * in current depth.
	 * 
	 */
	@Override
	public List<FolderEntryDescriptor> listFolder() throws IOException, UserActionRequiredException {
		List<FolderEntryDescriptor> list = null;

		try {
			
			if (this.url.getPath().isEmpty() || this.url.getPath().equals("/")) {
				list = getRootFolderEntriesDescriptiors();
			} else {
				list = getFolderEntriesDescriptiors();
			}
			
		} catch (CmisUnauthorizedException e) {
			WebappMessage webappMessage = new WebappMessage(WebappMessage
					.MESSAGE_TYPE_ERROR, ERROR_CODE, ERROR_MESSAGE, true);
			
			throw new UserActionRequiredException(webappMessage);
		}

		return list;
	}

	
	/**
	 * Add CmisObject url into FolderEntryDescriptor list
	 * 
	 * @param list
	 * @return 
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws UserActionRequiredException
	 */
	@VisibleForTesting
	public List<FolderEntryDescriptor> getFolderEntriesDescriptiors()
			throws MalformedURLException, CmisUnauthorizedException, UnsupportedEncodingException {
		List<FolderEntryDescriptor> list = new ArrayList<>();
			
		
		FileableCmisObject parent = null;

		// After connection we get ResourceController for generate URL!
		parent = (FileableCmisObject) connection
				.getCMISObject(url.toExternalForm());

		for (CmisObject obj : ((Folder) parent).getChildren()) {
			if (obj instanceof Document) {

				Boolean isPrivateWorkingCopy = ((Document) obj).isPrivateWorkingCopy();

				if (isPrivateWorkingCopy != null) {
					if (isPrivateWorkingCopy) {
						continue;
					}
				}
			}

			String parentPath = this.getURL().getPath();
			String entryUrl = CmisURLConnection.generateURLObject(obj,
					connection.getResourceController(url.toExternalForm()), parentPath);

			if (obj instanceof Folder) {
				entryUrl = entryUrl.concat("/");
			}

			list.add(new FolderEntryDescriptor(entryUrl));
		}
		folderEntryLogger(list);
		
		return list;
	}

	
	/**
	 * Add Repository url into FolderEntryDescriptor list
	 * 
	 * @param list
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws UserActionRequiredException
	 */
	@VisibleForTesting
	public List<FolderEntryDescriptor> getRootFolderEntriesDescriptiors()
			throws MalformedURLException, UnsupportedEncodingException, CmisUnauthorizedException {
		List<FolderEntryDescriptor> list = new ArrayList<>();
		
		List<Repository> reposList = connection.getCMISAccess()
				.connectToServerGetRepositories(serverUrl,
						connection.getUserCredentials());

		for (Repository repos : reposList) {
			String reposUrl = getRepositoryUrl(repos);
			list.add(new FolderEntryDescriptor(reposUrl));
		}

		folderEntryLogger(list);
		
		return list;
	}

	
	/**
	 * Generates custom URL for Repositories is used when URL path is empty
	 * 
	 * @param repo
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	private String getRepositoryUrl(Repository repo)
			throws UnsupportedEncodingException, MalformedURLException, CmisUnauthorizedException {
		
		StringBuilder urlb = new StringBuilder();

		// Connecting to Server to get host
		connection.getCMISAccess().connectToRepo(serverUrl, repo.getId(), connection.getUserCredentials());
		// Get server URL
		String originalProtocol = connection
				.getCMISAccess().getSession().getSessionParameters()
				.get(SessionParameter.ATOMPUB_URL);

		originalProtocol = URLEncoder.encode(originalProtocol, "UTF-8");
		
		urlb.append((CmisURLConnection.CMIS_PROTOCOL + "://"));
		urlb.append(originalProtocol).append("/");
		urlb.append(repo.getId()).append("/");

		return urlb.toString();
	}

	
	/**
	 * Logger for EntryDescriptions
	 * 
	 * @param list
	 */
	private void folderEntryLogger(List<FolderEntryDescriptor> list) {
		int i = 0;
		for (FolderEntryDescriptor fed : list) {
			logger.info(++i + ": " + fed.getAbsolutePath());
		}
	}
}
