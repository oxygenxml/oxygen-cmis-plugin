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
import java.util.List;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.ecss.extensions.api.webapp.WebappMessage;
import ro.sync.ecss.extensions.api.webapp.plugin.FilterURLConnection;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;
import ro.sync.net.protocol.FolderEntryDescriptor;

public class CmisBrowsingURLConnection extends FilterURLConnection {
	private static final Logger logger = Logger.getLogger(CmisBrowsingURLConnection.class.getName());

	// PRIVATE RESOURCES
	private CmisURLConnection cuc;
	private URL serverUrl;

	// CONSTRUCTOR
	public CmisBrowsingURLConnection(URLConnection delegateConnection, URL serverUrl) {
		super(delegateConnection);
		this.cuc = (CmisURLConnection) delegateConnection;
		this.serverUrl = serverUrl;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			return super.getInputStream();
		} catch (CmisUnauthorizedException e) {
			WebappMessage webappMessage = new WebappMessage(2, "401", "Invalid username or password!", true);
			throw new UserActionRequiredException(webappMessage);
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		try {
			return super.getOutputStream();
		} catch (CmisUnauthorizedException e) {
			WebappMessage webappMessage = new WebappMessage(2, "401", "Invalid username or password!", true);
			throw new UserActionRequiredException(webappMessage);
		}
	}

	/**
	 * Overrided listFolder method for WebAuthor
	 */
	@Override
	public List<FolderEntryDescriptor> listFolder() throws IOException, UserActionRequiredException {
		List<FolderEntryDescriptor> list = new ArrayList<FolderEntryDescriptor>();
		logger.info("listFolder() => " + url);

		try {
			if (this.url.getPath().isEmpty() || this.url.getPath().equals("/")) {
				rootEntryMethod(list);
			} else {
				entryMethod(list);
			}
		} catch (CmisUnauthorizedException e) {
			WebappMessage webappMessage = new WebappMessage(2, "401", "Invalid username or password!", true);
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
	public void entryMethod(List<FolderEntryDescriptor> list)
			throws MalformedURLException, CmisUnauthorizedException, UnsupportedEncodingException {
		FileableCmisObject parent = null;

		// After connection we get ResourceController for generate URL!
		parent = (FileableCmisObject) cuc.getCMISObject(url.toExternalForm());

		if (cuc.getResourceController(url) == null) {
			logger.info("entryMethod() => ResourceController is null!");
		}

		logger.info("entryMethod() => " + parent.getName());

		for (CmisObject obj : ((Folder) parent).getChildren()) {
			String entryUrl = CmisURLConnection.generateURLObject(obj, cuc.getResourceController(url));
			entryUrl = entryUrl.concat((obj instanceof Folder) ? "/" : "");
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
	public void rootEntryMethod(List<FolderEntryDescriptor> list)
			throws MalformedURLException, UnsupportedEncodingException, CmisUnauthorizedException {
		List<Repository> reposList = cuc.getCMISAccess().connectToServerGetRepositories(serverUrl, cuc.getUserCredentials());

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
	public String generateRepoUrl(Repository repo)
			throws UnsupportedEncodingException, MalformedURLException, CmisUnauthorizedException {
		StringBuilder urlb = new StringBuilder();
		
		// Connecting to Server to get host
		cuc.getCMISAccess().connectToRepo(serverUrl, repo.getId(), cuc.getUserCredentials());
		// Get server URL
		String originalProtocol = cuc.getCMISAccess().getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);

		originalProtocol = URLEncoder.encode(originalProtocol, "UTF-8");
		urlb.append((CmisURLConnection.CMIS_PROTOCOL + "://")).append(originalProtocol).append("/");
		urlb.append(repo.getId()).append("/");

		return urlb.toString();
	}

	/**
	 * 
	 * @param list
	 */
	public void folderEntryLogger(List<FolderEntryDescriptor> list) {
		int i = 0;
		for (FolderEntryDescriptor fed : list) {
			logger.info(++i + ") => " + fed.getAbsolutePath());
		}
	}
}
