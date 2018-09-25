package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

/**
 * Describes the cancel checkout action on a folder by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CancelCheckoutFolderAction extends AbstractAction {
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(CancelCheckoutFolderAction.class);
  private final transient ResourceController resourceController;

  // The resource that will receive
  private transient IResource resource = null;
  private transient IResource currentParent = null;
  private transient ResourcesBrowser itemsPresenter = null;

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * @param itemsPresenter
   * @param currentParent
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CancelCheckoutFolderAction(IResource resource, IResource currentParent, ResourcesBrowser itemsPresenter) {
    super("Cancel check out");

    // Set logger level

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
    this.resourceController = CMISAccess.getInstance().createResourceController();

    setEnabled(checkCanCancelCheckoutFolder(resource));

  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the folder using the recursion.
   * 
   * <Code>cancelCheckoutFolder</Code> will be called whenever a folder child
   * folder will be encountered, otherwise the cancel checkout will precedeF
   * 
   * @param e
   * @exception org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    cancelCheckoutFolder(resource);

    this.enabled = false;

    if (currentParent.getId().equals("#search.results")) {
      currentParent.refresh();

    } else {
      currentParent.refresh();
      itemsPresenter.presentResources(currentParent);
    }
  }

  /**
   * Helper method to iterate and commit the <Code> cancelCheckOut</Code>
   * 
   * @param resource
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  private void cancelCheckoutFolder(IResource resource) {

    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    if (childrenIterator != null) {

      // While has a child, add to the model
      while (childrenIterator.hasNext()) {

        commitCancelCheckout(childrenIterator);
      }
    }
  }

  /**
   * Does the cancel checkout on the resource whether is a folder or a document
   * using the recursion.
   * 
   * @param childrenIterator
   */
  private void commitCancelCheckout(Iterator<IResource> childrenIterator) {
    Document pwc;
    String pwcId;
    DocumentImpl pwcDoc;
    // Get the next child
    IResource iResource = childrenIterator.next();

    // Check if it's a custom type interface FolderImpl
    if (iResource instanceof FolderImpl) {

      // Call the helper method used for recursion
      cancelCheckoutFolder(iResource);

    } else if (iResource instanceof DocumentImpl) {

      // If it is a document type of custom interface
      try {
        DocumentImpl childResource = ((DocumentImpl) iResource);
        // Check if it's a checkout document
        if (childResource.isCheckedOut() && !childResource.isPrivateWorkingCopy()) {
          // Get the PWC id
          pwcId = childResource.getVersionSeriesCheckedOutId();

          if (pwcId != null) {
            logger.debug("Document = " + childResource.getDisplayName());
            logger.debug("PWC ID to cancel= " + pwcId);
            pwc = (Document) resourceController.getSession().getObject(pwcId);
            pwcDoc = new DocumentImpl(pwc);

            pwcDoc.cancelCheckOut();

          } else if (childResource.isPrivateWorkingCopy()) {
            childResource.cancelCheckOut();

          }
        }
      } catch (Exception ev) {
        // Show the exception if there is one
        logger.error("Exception action ", ev);
      }
    }
  }

  /**
   * Iterates each element whether, is a folder or a doc. If it's a folder it
   * will be iterated with recursion otherwise if it's a document it needs to
   * pass the check method.
   * 
   * @param resource
   * @return Whether the cancel checkout can be applied on the resources of the
   *         resource given.
   */
  private boolean checkCanCancelCheckoutFolder(IResource resource) {
    boolean canCancel = false;

    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    if (childrenIterator != null) {

      // While has a child, add to the model. It's enough one doc to be checkout
      // to enable the action
      while (childrenIterator.hasNext() && !canCancel) {

        // Get the next child
        IResource iResource = childrenIterator.next();

        // Check if it's a custom type interface FolderImpl
        if (iResource instanceof FolderImpl) {

          // Call the helper method used for recursion
          canCancel = checkCanCancelCheckoutFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {
          canCancel = checkDocument(iResource);
        }
      }
    }
    return canCancel;
  }

  /**
   * Checks if a the cancel checkout action is available for current user
   * 
   * @param canCancel
   * @param iResource
   * @return
   */
  private boolean checkDocument(IResource iResource) {
    DocumentImpl doc;
    String pwcId;
    boolean hasPwc;
    boolean canUserCancelCheckout;
    Document pwc;
    DocumentImpl pwcDoc;
    boolean canCancel = false;

    // If it is a document type of custom interface
    try {
      doc = ((DocumentImpl) iResource);

      // Check if it's a PWC
      if (doc.isCheckedOut() && !doc.isPrivateWorkingCopy()) {

        hasPwc = false;
        canUserCancelCheckout = false;

        // Get the PWC id
        pwcId = doc.getVersionSeriesCheckedOutId();

        // If has a PWC id
        if (pwcId != null) {
          hasPwc = true;

          // Get the pwc
          pwc = (Document) resourceController.getSession().getObject(pwcId);
          pwcDoc = new DocumentImpl(pwc);

          // Allow cancelCheckout
          canUserCancelCheckout = pwcDoc.canUserCancelCheckout();
        }
        canCancel = canUserCancelCheckout && hasPwc;

      }

    } catch (Exception ev) {

      // Show the exception if there is one
      logger.error("Exception check", ev);
    }
    return canCancel;
  }
}
