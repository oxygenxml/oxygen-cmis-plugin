package com.oxygenxml.cmis.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

import com.google.common.annotations.VisibleForTesting;
import com.oxygenxml.cmis.core.CmisCredentials;
import com.oxygenxml.cmis.core.CmisURL;
import com.oxygenxml.cmis.core.RepositoryInfo;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.TokenCredentials;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.action.CmisActionsUtills;

import lombok.extern.slf4j.Slf4j;
import ro.sync.ecss.extensions.api.webapp.WebappMessage;
import ro.sync.ecss.extensions.api.webapp.plugin.FilterURLConnection;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;
import ro.sync.net.protocol.FolderEntryDescriptor;

@Slf4j
public class CmisBrowsingURLConnection extends FilterURLConnection {

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
			Optional<String> objectIdOpt = CmisActionsUtills.getVersionId(url);
			if (objectIdOpt.isPresent()) {
				log.debug("Old ver. InputStream.");
				return getOlderVersionInputStream(objectIdOpt.get());
			}

			return super.getInputStream();

		} catch (CmisUnauthorizedException e) {
			WebappMessage webappMessage = new WebappMessage(WebappMessage
					.MESSAGE_TYPE_ERROR, ERROR_CODE, ERROR_MESSAGE, true);
			
			throw new UserActionRequiredException(webappMessage);
		} catch (CmisObjectNotFoundException e) {
		  throw new FileNotFoundException(e.getMessage());
    }
	}

	
	/**
	 * If document is an older version, we get InputStream
	 * of this document selected by id.
	 * 
	 * @return InputStream of older version document.
	 * @throws IOException 
	 */
	private InputStream getOlderVersionInputStream(String objectId) throws IOException {
		String connectionUrl = this.url.toExternalForm()
				.replace(this.url.getQuery(), "");

		connectionUrl = connectionUrl.replace("?", "");
		Document document = (Document) connection
				.getResourceController(connectionUrl).getCmisObj(objectId);

		return getVersionInputStream(document, objectId);
	}
	
	/**
	 * Returns the InputStream of the document. If the response comes from a SharePoint server
	 * the URL is modified to point to the correct object ID. Some SP implementations point
	 * to the latest version o the document instead of requested version
	 * 
	 * @param document the document
	 * @param objectId the id of the document
	 * @return the input stream to read the document
	 * @throws IOException
	 */
	private InputStream getVersionInputStream(Document document, String objectId) throws IOException {
	  String contentUrl = document.getContentUrl();

	  boolean isSharePoint = connection.getCMISAccess().isSharePoint();
	  if(isSharePoint && contentUrl.contains("objectID=")) {
	    contentUrl =  contentUrl.replaceAll("objectID=[^&]+", "objectID="+ objectId);
	    
      URL conn = new URL(contentUrl);
     
      CmisCredentials credentials = this.connection.getUserCredentials();
      URLConnection uc = conn.openConnection();
      if (credentials instanceof UserCredentials) {
        UserCredentials userCredentials = (UserCredentials) credentials;
        String userpass = userCredentials.getUsername() + ":" + userCredentials.getPassword();
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
        uc.setRequestProperty ("Authorization", basicAuth);
      } else {
        TokenCredentials tokenCredentials = (TokenCredentials) credentials;
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(tokenCredentials.getToken().getBytes()));
        uc.setRequestProperty ("Authorization", basicAuth);
      }
      
      return uc.getInputStream();
    }
    
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
		} catch (CmisObjectNotFoundException e) {
		  throw new FileNotFoundException(this.url.getPath());
		}

		return list;
	}

	
	/**
	 * Get CmisObjects URL and put it in list.
	 * 
	 * @return List<FolderEntryDescriptor> list.
	 * @throws MalformedURLException
	 */
	@VisibleForTesting
	public List<FolderEntryDescriptor> getFolderEntriesDescriptiors() throws MalformedURLException {
		List<FolderEntryDescriptor> list = new ArrayList<>();
			
		FileableCmisObject parent = null;

		// After connection we get ResourceController for generate URL!
		parent = (FileableCmisObject) connection
				.getCMISObject(url.toExternalForm());

		String externalForm = url.toExternalForm();
		ResourceController resourceController = connection.getResourceController(externalForm);

		Folder folder = (Folder) parent;
    for (CmisObject obj : folder.getChildren()) {
			if (obj instanceof Document) {
				Document doc = (Document) obj;
				Boolean isPrivateWorkingCopy = doc.isPrivateWorkingCopy();

				if (isPrivateWorkingCopy != null && isPrivateWorkingCopy) {
					continue;
				}
				// In case if isPrivateWorkingCopy is null.
				if (isPrivateWorkingCopy == null && checkPWCForAlfresco(doc)) {
					continue;
				}
			}

			String entryUrl = CmisURLConnection.generateURLObject(folder, obj, resourceController);

			list.add(new FolderEntryDescriptor(entryUrl));
		}
		
		return list;
	}

	/**
	 * If isPrivateWorkingCopy result is null we check document using
	 * isVersionSeriesPrivateWorkingCopy. 
	 * (In case if server doesn't support isPWC, ex. Alfresco).
	 * 
	 * @param doc Current document.
	 * @return true if is Version Series PWC else false.
	 */
	private boolean checkPWCForAlfresco(Document doc) {
		Boolean isVersionSeriesPWC = doc.isVersionSeriesPrivateWorkingCopy();
		return isVersionSeriesPWC != null && isVersionSeriesPWC;
	}

	/**
	 * Get repositories URLs and put it in list.
	 * 
	 * @return List<FolderEntryDescriptor> list.
	 */
	@VisibleForTesting
	public List<FolderEntryDescriptor> getRootFolderEntriesDescriptiors() {
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
	private String getRepositoryUrl(Repository repo) {
		// Connecting to Server to get host
		connection.getCMISAccess().connectToRepo(serverUrl, repo.getId(), connection.getUserCredentials());
		// Get server URL
		String atomPubUrlStr = connection
				.getCMISAccess().getSession().getSessionParameters()
				.get(SessionParameter.ATOMPUB_URL);
	    try {
	      return CmisURL.ofRepoWithName(new URL(atomPubUrlStr), new RepositoryInfo(repo.getId(), repo.getName())).toExternalForm();
	      
	      } catch (MalformedURLException e) {
	        // Cannot happen - the URL was already used by to retrieve data from the server.
	        throw new RuntimeException(e);
	      }
	}
}
