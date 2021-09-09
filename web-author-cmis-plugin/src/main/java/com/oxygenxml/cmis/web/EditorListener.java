package com.oxygenxml.cmis.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.access.EditingSessionContext;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.access.WebappEditingSessionLifecycleListener;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.ReadOnlyReason;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;


public class EditorListener implements WorkspaceAccessPluginExtension {

	private static final Logger logger = Logger.getLogger(EditorListener.class.getName());

	private PluginResourceBundle rb;

	private URL url;

	private String urlWithoutContextId;
	
	@VisibleForTesting
	UserCredentials credentials;
	
	@VisibleForTesting
	CmisURLConnection connection;

	private Document document;
	
	@Override
	public void applicationStarted(StandalonePluginWorkspace pluginWorkspaceAccess) {

		WebappPluginWorkspace webappPluginWorkspace = (WebappPluginWorkspace) pluginWorkspaceAccess;
		webappPluginWorkspace.addEditingSessionLifecycleListener(new WebappEditingSessionLifecycleListener() {

			@Override
			public void editingSessionStarted(String sessionId, AuthorDocumentModel documentModel) {
			  EditorListener.this.editingSessionStarted(webappPluginWorkspace, documentModel);
			}
		});
	}

	/**
	 * Receive some information about document to client-side code.
	 * 
	 * @param webappPluginWorkspace
	 * @param documentModel
	 */
	@VisibleForTesting
	public void editingSessionStarted(WebappPluginWorkspace webappPluginWorkspace, AuthorDocumentModel documentModel) {
		AuthorAccess authorAccess = documentModel.getAuthorAccess();

		// Get URL and ContextID for CmisURLConnection
		url = authorAccess.getEditorAccess().getEditorLocation();
		
		if (url == null || !url.getProtocol().equals("cmis")) {
		  return;
		}
		
		urlWithoutContextId = url.getProtocol() + "://" + url.getHost() + url.getPath();
		
		String contextId = url.getUserInfo();
		
		credentials = CredentialsManager.INSTANCE.getCredentials(contextId);
		
		connection = new CmisURLConnection(url, new CMISAccess(), credentials);

		rb = webappPluginWorkspace.getResourceBundle();

		try {
			logger.info("EditorListener was loaded!");

			if (url.getQuery() != null && url.getQuery().contains(EditorOption.OLD_VERSION.getValue())) {
				
				setOldVersionsOptions(webappPluginWorkspace, documentModel);

			} else {
				document = (Document) connection.getCMISObject(urlWithoutContextId);

				// If server doesn't support private working copy and check in comments features
				// we disable this actions in editor.
				if(document.getCheckinComment() != null) {
					documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
					.setPseudoClass(EditorOption.SUPPORTS_COMMIT_MESSAGE.getValue());
				}
				
				setEditorsOptions(documentModel);
			}

		} catch (CmisUnauthorizedException | CmisObjectNotFoundException | MalformedURLException e1) {
			logger.info(e1.getMessage());
		}
	}

	/**
	 * If Server or repository doesn't support versionable features
	 * we set Web Author's editor to non-versionable document options.
	 * 
	 * @param documentModel
	 * @param document
	 */
	private void setEditorsOptions(AuthorDocumentModel documentModel) {
		// If server doesn't support version control system
		// we disable this feature in editor.
		if (!document.isVersionable()) {
			documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
					.setPseudoClass(EditorOption.NON_VERSIONABLE.getValue());
		} else if (document.isVersionSeriesCheckedOut()) {
			//
			setVersionableOptions(documentModel);
		} else {
		  EditingSessionContext editingContext = documentModel.getAuthorAccess().getEditorAccess().getEditingContext();
		  
		  Object previewMode = editingContext.getAttribute("previewMode");
		  boolean isPreview = "true".equals(previewMode) || Boolean.TRUE.equals(previewMode);
		  
		  // on preview there is no checkout action.
			if (!isPreview && isCheckOutRequired() && !document.isVersionSeriesCheckedOut()) {
				documentModel.getAuthorAccess().getEditorAccess()
						.setReadOnly(new ReadOnlyReason(rb.getMessage(TranslationTags.CHECK_OUT_REQUIRED)));
			}
		}
	}

	/**
	 * Set Web Author's editor to versionable document options.
	 * 
	 * @param documentModel
	 * @param credentials
	 * @param rb
	 * @param document
	 */
	private void setVersionableOptions(AuthorDocumentModel documentModel) {
		String versionSeriesCheckedOutBy = document.getVersionSeriesCheckedOutBy();
		
    ResourceController resourceController = connection.getCMISAccess().createResourceController();

    Document pwcDoc = document;
    if (document.getVersionSeriesCheckedOutId() != null) {
      String pwcId = document.getVersionSeriesCheckedOutId();
      pwcDoc = (Document) resourceController.getSession().getObject(pwcId);
    }

    boolean canEditDocument = canEditDocument(pwcDoc);

    if (canEditDocument) {
      documentModel.getAuthorDocumentController()
          .getAuthorDocumentNode()
          .getRootElement()
          .setPseudoClass(EditorOption.IS_CHECKED_OUT.getValue());

    } else {
      documentModel.getAuthorAccess()
          .getEditorAccess()
          .setReadOnly(new ReadOnlyReason(
              MessageFormat.format(rb.getMessage(TranslationTags.CHECKED_OUT_BY), versionSeriesCheckedOutBy)));
      documentModel.getAuthorDocumentController()
          .getAuthorDocumentNode()
          .getRootElement()
          .setPseudoClass(EditorOption.LOCKED.getValue());
    }
	}

	@VisibleForTesting
	boolean canEditDocument(Document doc) {
	  Boolean canSetContentStream = doc.hasAllowableAction(Action.CAN_SET_CONTENT_STREAM);
	  boolean isSharePoint = connection.getCMISAccess().isSharePoint();
	  
	  String versionSeriesCheckedOutBy = doc.getVersionSeriesCheckedOutBy();

	  // With SharePoint the value of the versionSeriesCheckedOutBy attribute is the display name
	  // of the user and not the login name. Therefore when connected to SharePoint we check
	  // if the logged in user can set content stream for the document
	  // For other CMSes we verify that the user who checked out the document is the logged in user 
	  return (canSetContentStream && isSharePoint) || credentials.getUsername().equals(versionSeriesCheckedOutBy);
	}
	
	/**
	 * Set Web Author's editor to older version 
	 * of document options.
	 * 
	 * @param webappPluginWorkspace
	 * @param documentModel
	 * @param url
	 * @param urlWithoutContextId
	 * @param connection
	 * @param rb
	 * @throws MalformedURLException
	 */
	private void setOldVersionsOptions(WebappPluginWorkspace webappPluginWorkspace, AuthorDocumentModel documentModel)
			throws MalformedURLException {
		
		String objectId = getOldVersionDocumentId(url);
		Document oldDoc = (Document) connection.getResourceController(urlWithoutContextId)
							.getCmisObj(objectId);

		String language = webappPluginWorkspace.getUserInterfaceLanguage();
		SimpleDateFormat df = new SimpleDateFormat("d MMM yyyy HH:mm:ss", new Locale(language));
		
		Date lastMod = oldDoc.getLastModificationDate().getTime();
		
		documentModel.getAuthorAccess().getEditorAccess().setReadOnly(
				new ReadOnlyReason(rb.getMessage(TranslationTags.OLD_VER_WARNING) + " : " + df.format(lastMod)));
		documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
				.setPseudoClass(EditorOption.OLD_VERSION.getValue());
		
		if(oldDoc.getCheckinComment() != null) {
			documentModel.getAuthorDocumentController().getAuthorDocumentNode().getRootElement()
			.setPseudoClass(EditorOption.SUPPORTS_COMMIT_MESSAGE.getValue());
		}
	}
	
	/**
	 * Get ObjectId from query part of our custom URL.
	 * 
	 * @param url
	 * @return objectId of old version of document.
	 */
	private String getOldVersionDocumentId(URL url) {
		HashMap<String, String> queryPart = new HashMap<>();
		
		for(String pair : url.getQuery().split("&")) {
			int index = pair.indexOf('=');
			queryPart.put(pair.substring(0, index), pair.substring(index + 1));
		}
		
		return queryPart.get(EditorOption.OLD_VERSION.getValue());
	}
	
	/**
	 * 
	 * @return true if checked out is required
	 */
	public static boolean isCheckOutRequired() {
		WSOptionsStorage optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
		String optionValue = optionsStorage.getOption(CmisPluginConfigExtension.CHECKOUT_REQUIRED, "on");
		return "on".equals(optionValue);
	}

	@Override
	public boolean applicationClosing() {
		return true;
	}
}
