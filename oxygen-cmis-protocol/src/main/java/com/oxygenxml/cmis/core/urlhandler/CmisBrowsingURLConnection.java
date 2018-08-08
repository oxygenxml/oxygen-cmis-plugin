package com.oxygenxml.cmis.core.urlhandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import ro.sync.ecss.extensions.api.webapp.plugin.FilterURLConnection;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;
import ro.sync.net.protocol.FolderEntryDescriptor;

public class CmisBrowsingURLConnection extends FilterURLConnection {
	private static final Logger logger = Logger.getLogger(CmisBrowsingURLConnection.class.getName());

	//PRIVATE RESOURCES
	private CmisURLConnection cuc;
	private ResourceController ctrl;

	//CONSTRUCTOR
	public CmisBrowsingURLConnection(URLConnection delegateConnection) {
		super(delegateConnection);
		this.cuc = (CmisURLConnection) delegateConnection;
	}

	/**
	 * Overrided listFolder method for WebAuthor
	 */
	@Override
	public List<FolderEntryDescriptor> listFolder() throws IOException, UserActionRequiredException {
		List<FolderEntryDescriptor> list = new ArrayList<FolderEntryDescriptor>();
		logger.info("LIST FOLDER Method ---> " + url);

		if (this.url.getPath().isEmpty() || this.url.getPath().equals("/")) {
			rootEntryMethod(list);
		} else {
			entryMethod(list);
		}

		return list;
	}
	
	/**
	 * Add CmisObject url into FolderEntryDescriptor list
	 * @param list
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public void entryMethod(List<FolderEntryDescriptor> list)
			throws MalformedURLException, UnsupportedEncodingException {
		FileableCmisObject parent = (FileableCmisObject) cuc.getCMISObject(url.toExternalForm());

		// After connection we get ResourceController for generate URL!
		ctrl = cuc.getCtrl(url);

		if (ctrl == null) {
			logger.info("ResourceController is null!");
		}

		logger.info("OBJ NAME ---> " + parent.getName());

		for (CmisObject obj : ((Folder) parent).getChildren()) {
			String entryUrl = CmisURLConnection.generateURLObject(obj, ctrl);
			entryUrl = entryUrl.concat((obj instanceof Folder) ? "/" : "");
			list.add(new FolderEntryDescriptor(entryUrl));
		}

		folderEntryLogger(list);
	}

	/**
	 * Add Repository url into FolderEntryDescriptor list
	 * @param list
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public void rootEntryMethod(List<FolderEntryDescriptor> list)
			throws MalformedURLException, UnsupportedEncodingException {
		logger.info("TUT!! " + url.toExternalForm());

		List<Repository> reposList = cuc.getReposList(url);

		for (Repository repos : reposList) {
			String reposUrl = generateRepoUrl(repos);
			list.add(new FolderEntryDescriptor(reposUrl));
		}
		
		folderEntryLogger(list);
	}

	/**
	 * Generates custom URL for Repositories 
	 * is used when URL path is empty
	 * @param repo
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String generateRepoUrl(Repository repo) throws UnsupportedEncodingException {
		StringBuilder urlb = new StringBuilder();

		// Get server URL
		String originalProtocol = CMISAccess.getInstance().getSession().getSessionParameters()
				.get(SessionParameter.ATOMPUB_URL);

		originalProtocol = URLEncoder.encode(originalProtocol, "UTF-8");

		urlb.append(("cmis" + "://")).append(originalProtocol).append("/");
		urlb.append(CMISAccess.getInstance().getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID));
		urlb.append("/");

		return urlb.toString();
	}
	
	/**
	 * 
	 * @param list
	 */
	public void folderEntryLogger(List<FolderEntryDescriptor> list) {
		// LOGGING
		int i = 0;
		for (FolderEntryDescriptor fed : list) {
			logger.info(++i + ") " + fed.getAbsolutePath());
		}
	}
}
