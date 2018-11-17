package com.oxygenxml.cmis.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
import com.oxygenxml.cmis.core.CmisURL;
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
			String cmisQuery = this.url.getQuery();
			
			if (cmisQuery != null && cmisQuery.contains(CmisAction.OLD_VERSION.getValue())) {
				logger.debug("Old ver. InputStream.");
				return getOlderVersionInputStream();
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
	public List<FolderEntryDescriptor> listFolder() throws IOException {
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
	 * Get CmisObjects URL and put it in list.
	 * 
	 * @return List<FolderEntryDescriptor> list.
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws UserActionRequiredException
	 */
	@VisibleForTesting
	public List<FolderEntryDescriptor> getFolderEntriesDescriptiors() throws MalformedURLException {
		List<FolderEntryDescriptor> list = new ArrayList<>();
			
		FileableCmisObject parent = null;

		// After connection we get ResourceController for generate URL!
		parent = (FileableCmisObject) connection
				.getCMISObject(url.toExternalForm());

		for (CmisObject obj : ((Folder) parent).getChildren()) {
			if (obj instanceof Document) {

				Boolean isPrivateWorkingCopy = ((Document) obj).isPrivateWorkingCopy();

				if (isPrivateWorkingCopy != null && isPrivateWorkingCopy) {
					continue;
				}
			}

			String parentPath = CmisURL.parse(this.getURL().toExternalForm()).getFolderPath();
			String entryUrl = CmisURLConnection.generateURLObject(obj,
					connection.getResourceController(url.toExternalForm()), parentPath);

			if (obj instanceof Folder) {
				entryUrl = entryUrl.concat("/");
			}

			list.add(new FolderEntryDescriptor(entryUrl));
		}
		
		return list;
	}

	
	/**
	 * Get repositories URLs and put it in list.
	 * 
	 * @return List<FolderEntryDescriptor> list.
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws UserActionRequiredException
	 */
	@VisibleForTesting
	public List<FolderEntryDescriptor> getRootFolderEntriesDescriptiors() throws UnsupportedEncodingException {
		List<FolderEntryDescriptor> list = new ArrayList<>();
		
		List<Repository> reposList = connection.getCMISAccess()
				.connectToServerGetRepositories(serverUrl,
						connection.getUserCredentials());

		for (Repository repos : reposList) {
			String reposUrl = getRepositoryUrl(repos);
			list.add(new FolderEntryDescriptor(reposUrl));
		}
		
		return list;
	}

	
	/**
	 * Generates custom URL for Repositories.
	 * 
	 * @param repo
	 * @return Repository String URL.
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	private String getRepositoryUrl(Repository repo) throws UnsupportedEncodingException {
		// Connecting to Server to get host
		connection.getCMISAccess().connectToRepo(serverUrl, repo.getId(), connection.getUserCredentials());
		// Get server URL
		String atomPubUrlStr = connection
				.getCMISAccess().getSession().getSessionParameters()
				.get(SessionParameter.ATOMPUB_URL);
    try {
      return CmisURL.ofRepo(new URL(atomPubUrlStr), repo.getId()).toExternalForm();
    } catch (MalformedURLException e) {
      // Cannot happen - the URL was already used by to retrieve data from the server.
      throw new RuntimeException(e);
    }
	}
}
