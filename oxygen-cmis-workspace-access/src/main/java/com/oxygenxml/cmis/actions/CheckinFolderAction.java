package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;

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
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes the cancel checkout action on a folder by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CheckinFolderAction extends AbstractAction {
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(CheckinFolderAction.class);
  // The resource that will receive
  private transient IResource resource = null;
  private transient IResource currentParent = null;
  private transient ResourcesBrowser itemsPresenter = null;
  private static ResourceController resourceController = CMISAccess.getInstance().createResourceController();

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * @param itemsPresenter
   * @param currentParent
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CheckinFolderAction(IResource resource, IResource currentParent, ResourcesBrowser itemsPresenter) {

    super("Check in");

    // Set logger level
    

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

    setEnabled(checkCanCheckinFolder(resource));

  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the folder using the recursion.
   * 
   * <Code>checkinFolder</Code> will be called whenever a folder child folder
   * will be encountered, otherwise the checkin will precede
   * 
   * @param e
   * @exception org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

    String commitMessage;
    String versioningState;
    boolean majorCheckin = false;
    int result;
    // A comment in mandatory
    do {
      // Create the input dialog
      CheckinDocDialog inputDialog = new CheckinDocDialog((JFrame) pluginWorkspace.getParentFrame(),
          resource.getDisplayName());
      commitMessage = inputDialog.getCommitMessage();
      versioningState = inputDialog.getVersioningState();
      result = inputDialog.getResult();

      if (result == 0) {
        break;
      }
    } while (commitMessage == null);

    // Only if the action was not canceled
    if (result != 0) {
      majorCheckin = versioningState.equals("MAJOR");
    }

    checkinFolder(resource, commitMessage, majorCheckin, result);

    if (currentParent.getId().equals("#search.results")) {
      currentParent.refresh();

    } else {
      currentParent.refresh();
      itemsPresenter.presentResources(currentParent);
    }
  }

  /**
   * Helper method to iterate and commit the <Code> checkinFolder</Code> using
   * the recursion till the child has new children. Shows the input dialog while
   * the user did not canceled or entered a message. Then commit the check in if
   * we had a positive result.
   * 
   * @param resource
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  private void checkinFolder(IResource resource, String commitMessage, boolean majorCheckin, int result) {

    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    // Check if there are no children
    if (childrenIterator != null) {

      // While has a child, add to the model
      while (childrenIterator.hasNext()) {

        // Get the next child
        IResource iResource = childrenIterator.next();

        // Check if it an instance of custom interface Folder
        if (iResource instanceof FolderImpl) {

          // Call the checkinFolder again recursively
          checkinFolder(iResource, commitMessage, majorCheckin, result);

        } else if (iResource instanceof DocumentImpl) {
          // If it's a document try <Code>checkIn</Code>
          // Only if the action was not canceled
          
          DocumentImpl doc = (DocumentImpl) iResource;
          if (result != 0 && doc.isCheckedOut() && !doc.isPrivateWorkingCopy()) {
            // Try to <Code>checkIn</Code>
            commitCheckIn(resource, commitMessage, doc, majorCheckin);
          }

        }
      }
    }
  }

  /**
   * Commits the actual check-in by getting the PWC of the document. Removes the
   * old model and add the new one.
   * 
   * @param resource
   * @param commitMessage
   * @param pwcDoc
   * @param doc
   * @param majorCheckin
   */
  private void commitCheckIn(IResource resource, String commitMessage, DocumentImpl doc, boolean majorCheckin) {
    ObjectId res;

    try {
      String pwcId = doc.getVersionSeriesCheckedOutId();

      DocumentImpl pwcDoc = null;
      if (pwcId != null) {

        // Get the pwc
        Document pwc = (Document) resourceController.getSession().getObject(pwcId);
        pwcDoc = new DocumentImpl(pwc);

      }

      if (pwcDoc != null) {
        // Commit the <Code>checkIn</Code> and Get the ObjectId
        res = pwcDoc.checkIn(majorCheckin, commitMessage);

        // If we are in the search
        if (currentParent.getId().equals("#search.results")) {

          // Remove from model current resource
          ((IFolder) currentParent).removeFromModel(resource);

          // Add the new one to the model
          Document checkedInResource = resourceController.getDocument(res.getId());
          ((IFolder) currentParent).addToModel(checkedInResource);

        } else {
          currentParent.refresh();
          itemsPresenter.presentResources(currentParent);

        }
      }

    } catch (Exception ev) {

      // Show the exception if there is one
      logger.error("Exception action ", ev);
    }

  }

  /**
   * Checks if a check in is possible for the folder and the document. Folder is
   * iterated recursively.
   * 
   * @param resource
   * @return
   */
  private boolean checkCanCheckinFolder(IResource resource) {
    boolean canCheckin = false;
    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    if (childrenIterator != null) {

      // While has a child, add to the model
      while (childrenIterator.hasNext() && !canCheckin) {

        // Get the next child
        IResource iResource = childrenIterator.next();

        // Check if it's a custom type interface FolderImpl
        if (iResource instanceof FolderImpl) {

          // Call the helper method used for recursion
          canCheckin = checkCanCheckinFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {
          // If it is a document type of custom interface
          try {

            canCheckin = checkDocument( iResource);

          } catch (Exception ev) {

            // Show the exception if there is one
            logger.debug("Exception ", ev);
          }
        }
      }
    }
    return canCheckin;
  }

  /**
   * Checks if a the cancel checkout action is available for current user
   * 
   * @param canCancel
   * @param iResource
   * @return
   */
  private boolean checkDocument( IResource iResource) {
    DocumentImpl doc;
    String pwcId;
    boolean hasPwc;
    boolean canUserCheckin;
    Document pwc;
    DocumentImpl pwcDoc;

    boolean canCheckin = false;
    // If it is a document type of custom interface
    try {
      doc = ((DocumentImpl) iResource);

      // Check if it's a PWC
      if (doc.isCheckedOut() && !doc.isPrivateWorkingCopy()) {

        hasPwc = false;
        canUserCheckin = false;

        // Get the PWC id
        pwcId = doc.getVersionSeriesCheckedOutId();

        // If has a PWC id
        if (pwcId != null) {
          hasPwc = true;

          // Get the pwc
          pwc = (Document) resourceController.getSession().getObject(pwcId);
          pwcDoc = new DocumentImpl(pwc);

          // Allow cancelCheckout
          canUserCheckin = pwcDoc.canUserCheckin();
        }
        canCheckin = canUserCheckin && hasPwc;

      }

    } catch (Exception ev) {

      // Show the exception if there is one
      logger.error("Exception check", ev);
    }
    return canCheckin;
  }
}
