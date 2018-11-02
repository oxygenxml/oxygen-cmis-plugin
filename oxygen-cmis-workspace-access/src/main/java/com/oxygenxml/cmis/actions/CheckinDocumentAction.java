package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.log4j.Logger;

import com.oxygen.cmis.dialogs.CheckinDocDialog;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.plugin.TranslationResourceController;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes the check in action on a document by extending the AbstractAction
 * class
 * 
 * @author bluecc
 *
 */
public class CheckinDocumentAction extends AbstractAction {

  private static final String SEARCH_RESULTS_ID = "#search.results";
  private static final String VERSIONING_STATE_MAJOR = "MAJOR";
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(CheckinDocumentAction.class);
  private final transient ResourceController resourceController;

  // The resource that will receive
  private transient IResource resource = null;
  private transient IResource currentParent = null;
  private transient ResourcesBrowser itemsPresenter = null;

  private transient DocumentImpl pwcDoc;

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * @param itemsPresenter
   * @param currentParent
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CheckinDocumentAction(IResource resource, IResource currentParent, ResourcesBrowser itemsPresenter) {
    super(TranslationResourceController.getMessage("CHECK_IN_ACTION_TITLE"));

    this.resourceController = CMISAccess.getInstance().createResourceController();

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

    DocumentImpl doc = ((DocumentImpl) resource);
    String pwcId = null;
    boolean hasPwc = false;
    boolean canUserCheckin = false;

    // Check if the doc is checked-out and get the PWC id
    if (doc.isCheckedOut() && !doc.isPrivateWorkingCopy()) {
      pwcId = doc.getVersionSeriesCheckedOutId();
    }

    // If has a PWC id
    if (pwcId != null) {
      hasPwc = true;

      // Get the pwc
      Document pwc = (Document) resourceController.getSession().getObject(pwcId);
      pwcDoc = new DocumentImpl(pwc);

      // Allow cancelCheckout
      canUserCheckin = pwcDoc.canUserCheckin();
    }

    boolean canCheckin = canUserCheckin && doc.isCheckedOut() && hasPwc;
    setEnabled(canCheckin);
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the document
   * 
   * @param e
   * @exception org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
    int result = 0;
    String versioningState;
    String commitMessage;
    CheckinDocDialog inputDialog;

    // A comment in mandatory
    do {
      // Create the input dialog
      inputDialog = new CheckinDocDialog((JFrame) pluginWorkspace.getParentFrame(), resource.getDisplayName());
      commitMessage = inputDialog.getCommitMessage();
      versioningState = inputDialog.getVersioningState();
      result = inputDialog.getResult();

      if (result == 0) {
        break;
      }
    } while (commitMessage == null);

    // Only if the action was not canceled
    if (result != 0) {
      boolean majorCheckin = versioningState.equals(VERSIONING_STATE_MAJOR);

      // The id received of the object after the check in
      ObjectId res = null;

      // Cast to the custom interface to use its methods

      // Try to <Code>checkIn</Code>
      try {

        // Commit the <Code>checkIn</Code> and Get the ObjectId
        res = pwcDoc.checkIn(majorCheckin, commitMessage);

        if (currentParent.getId().equals(SEARCH_RESULTS_ID)) {
          ((IFolder) currentParent).removeFromModel(resource);

          Document checkedInResource = CMISAccess.getInstance().createResourceController().getDocument(res.getId());
          ((IFolder) currentParent).addToModel(checkedInResource);
        } else {
          currentParent.refresh();
          itemsPresenter.presentResources(currentParent);
        }

      } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException ev) {

        // Show the exception if there is one
        logger.error("Exception action ", ev);
      }
    }
  }
}
