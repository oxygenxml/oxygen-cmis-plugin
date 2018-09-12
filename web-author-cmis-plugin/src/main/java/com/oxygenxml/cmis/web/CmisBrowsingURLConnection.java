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
import com.oxygenxml.cmis.web.action.CmisActions;

import ro.sync.ecss.extensions.api.webapp.WebappMessage;
import ro.sync.ecss.extensions.api.webapp.plugin.FilterURLConnection;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;
import ro.sync.net.protocol.FolderEntryDescriptor;

public class CmisBrowsingURLConnection extends FilterURLConnection {
	private static final Logger logger = Logger.getLogger(CmisBrowsingURLConnection.class.getName());

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
				if(this.url.getQuery().contains(CmisActions.OLD_VERSION)) {
					
					HashMap<String, String> queryPart = new HashMap<>();

					for (String pair : url.getQuery().split("&")) {
						int index = pair.indexOf("=");
						queryPart.put(pair.substring(0, index), pair.substring(index + 1));
					}

					String objectId = queryPart.get(CmisActions.OLD_VERSION);
					String connectionUrl = this.url.toExternalForm().replace(this.url.getQuery(), "");

					connectionUrl = connectionUrl.replace("?", "");
					Document document = (Document) connection.getResourceController(connectionUrl).getCmisObj(objectId);

					return document.getContentStream().getStream();
				}
			}

			return super.getInputStream();

		} catch (CmisUnauthorizedException e) {
			WebappMessage webappMessage = new WebappMessage(WebappMessage.MESSAGE_TYPE_ERROR, "401",
					"Invalid username or password!", true);
			
			throw new UserActionRequiredException(webappMessage);
		}
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
			WebappMessage webappMessage = new WebappMessage(WebappMessage.MESSAGE_TYPE_ERROR, "401",
					"Invalid username or password!", true);
			
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
		List<FolderEntryDescriptor> list = new ArrayList<FolderEntryDescriptor>();
		logger.info("listFolder: " + url);

		try {
			if (this.url.getPath().isEmpty() || this.url.getPath().equals("/")) {
				rootEntryMethod(list);
			} else {
				entryMethod(list);
			}
		} catch (CmisUnauthorizedException e) {
			WebappMessage webappMessage = new WebappMessage(WebappMessage.MESSAGE_TYPE_ERROR, "401",
					"Invalid username or password!", true);
			
			throw new UserActionRequiredException(webappMessage);
		}

		return list;
	}

	/**
	 * Add CmisObject url into FolderEntryDescriptor list
	 * 
	 * @param list
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws UserActionRequiredException
	 */
	@VisibleForTesting
	public void entryMethod(List<FolderEntryDescriptor> list)
			throws MalformedURLException, CmisUnauthorizedException, UnsupportedEncodingException {
		FileableCmisObject parent = null;

		// After connection we get ResourceController for generate URL!
		parent = (FileableCmisObject) connection.getCMISObject(url.toExternalForm());

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
	public void rootEntryMethod(List<FolderEntryDescriptor> list)
			throws MalformedURLException, UnsupportedEncodingException, CmisUnauthorizedException {
		List<Repository> reposList = connection.getCMISAccess().connectToServerGetRepositories(serverUrl,
				connection.getUserCredentials());

		for (Repository repos : reposList) {
			String reposUrl = generateRepoUrl(repos);
			list.add(new FolderEntryDescriptor(reposUrl));
		}

		folderEntryLogger(list);
	}

	/**
	 * Generates custom URL for Repositories is used when URL path is empty
	 * 
	 * @param repo
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	private String generateRepoUrl(Repository repo)
			throws UnsupportedEncodingException, MalformedURLException, CmisUnauthorizedException {
		StringBuilder urlb = new StringBuilder();

		// Connecting to Server to get host
		connection.getCMISAccess().connectToRepo(serverUrl, repo.getId(), connection.getUserCredentials());
		// Get server URL
		String originalProtocol = connection.getCMISAccess().getSession().getSessionParameters()
				.get(SessionParameter.ATOMPUB_URL);

		originalProtocol = URLEncoder.encode(originalProtocol, "UTF-8");
		urlb.append((CmisURLConnection.CMIS_PROTOCOL + "://")).append(originalProtocol).append("/");
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
