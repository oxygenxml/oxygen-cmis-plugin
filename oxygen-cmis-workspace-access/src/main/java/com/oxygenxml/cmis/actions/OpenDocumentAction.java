package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.UIManager;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.CmisAccessSingleton;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.plugin.OptionsCMIS;
import com.oxygenxml.cmis.plugin.TranslationResourceController;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;

/**
 * Describes the delete folder action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class OpenDocumentAction extends AbstractAction {

  // Internal role
  private static final String ALLOW_EDIT_OPTION_FALSE = "false";

  private static final Logger logger = Logger.getLogger(OpenDocumentAction.class);

  // The resource to open
  private transient IResource resource = null;
  private transient IResource currentParent = null;
  private transient ResourcesBrowser itemsPresenter = null;

  /**
   * Constructor that gets the resource to open
   * 
   * @param resource
   */
  public OpenDocumentAction(IResource resource, IResource currentParent, ResourcesBrowser itemsPresenter) {
    /**
     * Logging.
     */
    // Set a name and a native icon
    super(TranslationResourceController.getMessage("OPEN_DOCUMENT_ACTION_TITLE"), UIManager.getIcon("Tree.openIcon"));

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

    setEnabled(((DocumentImpl) resource).canUserOpen());
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the document.
   * 
   * <b>This action will delete everything inside the folder (folders,
   * documents)</b>
   * 
   * @param e
   * @exception MalformedURLException
   *              , UnsupportedEncodingException
   * 
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    openDocumentPath();

  }

  public void openDocumentPath() {
    // Get the workspace of the plugin
    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

    boolean editable = false;
    DocumentImpl currDoc = (DocumentImpl) resource;
    String allowEditOption = pluginWorkspace.getOptionsStorage().getOption(OptionsCMIS.ALLOW_EDIT,
        ALLOW_EDIT_OPTION_FALSE);
    Boolean allowEditOriginal = Boolean.valueOf(allowEditOption);

    editable = canUserOpenDoc(editable, currDoc, allowEditOriginal);

    // -------Oxygen

    // Initialize the URL
    final String urlAsTring = CmisURLConnection.generateURLObject(currDoc.getDoc(),
        CmisAccessSingleton.getInstance().createResourceController());

    // Try opening in the Oxygen the URL
    try {

      pluginWorkspace.addEditorChangeListener(new WSEditorChangeListener() {
        @Override
        public void editorOpened(URL editorLocation) {
          logger.debug("Nothing to do here");
        }

        @Override
        public void editorClosed(URL editorLocation) {
          itemsPresenter.presentResources(currentParent);
          pluginWorkspace.removeEditorChangeListener(this, PluginWorkspace.MAIN_EDITING_AREA);
        }
      }, PluginWorkspace.MAIN_EDITING_AREA);

      if (pluginWorkspace.open(new URL(urlAsTring))) {

        // if null - image preview opened
        WSEditor editorAccess = pluginWorkspace.getEditorAccess(new URL(urlAsTring), PluginWorkspace.MAIN_EDITING_AREA);

        if (editorAccess != null) {
          editorAccess.setEditable(editable);
        } else {

          pluginWorkspace.openInExternalApplication(new URL(urlAsTring), true);

        }
      }

    } catch (MalformedURLException e1) {

      // Show the exception if there is one
      logger.debug("Exception ", e1);
    }

    // ------
  }

  /**
   * Checks if the user by checking if the document is checked out and the PWC
   * can be updated (doc cannot on the state of checked out) can open the
   * selected document.
   * 
   * @param editable
   * @param currDoc
   * @param allowEditOriginal
   * @return
   */
  private boolean canUserOpenDoc(boolean editable, DocumentImpl currDoc, Boolean allowEditOriginal) {
    ResourceController resourceController = CmisAccessSingleton.getInstance().createResourceController();
    String pwcId;
    if (currDoc.isVersionable()) {
      pwcId = currDoc.getVersionSeriesCheckedOutId();

      Document pwc = null;
      if (pwcId != null) {
        pwc = (Document) resourceController.getSession().getObject(pwcId);
      }
      if (pwc != null) {
        DocumentImpl pwcDoc = new DocumentImpl(pwc);

        if (currDoc.isCheckedOut() && pwcDoc.canUserUpdateContent()) {
          editable = true;
        } else {
          editable = allowEditOriginal;
        }
      }
    } else {
      editable = true;
    }
    logger.info("Is checked out? " + currDoc.isCheckedOut() + " Can user update? " + currDoc.canUserUpdateContent());
    return editable;
  }
}
