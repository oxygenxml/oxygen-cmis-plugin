package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.plugin.TranslationResourceController;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

/**
 * Describes the cancel checkout action on a folder by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CheckoutFolderAction extends AbstractAction {
  // Internal role
  private static final String SEARCH_RESULTS_ID = "#search.results";

  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(CheckoutFolderAction.class);

  // The resource that will receive
  private transient IResource resource = null;
  private transient IResource currentParent = null;
  private transient ResourcesBrowser itemsPresenter = null;

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * @param itemListView
   * @param currentParent
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CheckoutFolderAction(IResource resource, IResource currentParent, ResourcesBrowser itemsPresenter) {
    super(TranslationResourceController.getMessage("CMIS_CHECK_OUT"));

    // Set logger level

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

    setEnabled(checkCanCheckoutFolder(resource));
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the folder using the recursion.
   * 
   * <Code>checkoutFolder</Code> will be called whenever a folder child folder
   * will be encountered, otherwise the checkout will precede
   * 
   * @param e
   * @exception org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    checkoutFolder(resource);
    if (currentParent.getId().equals(SEARCH_RESULTS_ID)) {
      currentParent.refresh();

    } else {
      currentParent.refresh();
      itemsPresenter.presentResources(currentParent);
    }
  }

  /**
   * Helper method to iterate and commit the <Code> checkoutFolder</Code> using
   * the recursion till the child has new children
   * 
   * @param resource
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  private void checkoutFolder(IResource resource) {

    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    // If has children
    if (childrenIterator != null) {

      // While has a child, add to the model
      while (childrenIterator.hasNext()) {

        // Get the child
        IResource iResource = childrenIterator.next();

        // Check if it's an instance of custom interface Folder
        if (iResource instanceof FolderImpl) {
          // Call recursively if it's a folder
          checkoutFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {

          commitCheckout(iResource);
        }
      }
    }
  }

  /**
   * Commit the checkout
   * 
   * @param iResource
   */
  private void commitCheckout(IResource iResource) {
    try {
      // Commit the checkOut
      DocumentImpl doc = (DocumentImpl) iResource;

      if (!(doc.isCheckedOut() || doc.isPrivateWorkingCopy())) {
        doc.checkOut(doc.getDocType());
      }
    } catch (Exception ev) {

      // SHow the exception if there is one
      logger.debug("Exception ", ev);
    }
  }

  /**
   * Check if a check out can be applied
   * 
   * @param resource
   * @return
   */
  private boolean checkCanCheckoutFolder(IResource resource) {
    boolean canCheckout = false;
    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    if (childrenIterator != null) {

      // While has a child, add to the model
      while (childrenIterator.hasNext() && !canCheckout) {

        // Get the next child
        IResource iResource = childrenIterator.next();

        // Check if it's a custom type interface FolderImpl
        if (iResource instanceof FolderImpl) {

          // Call the helper method used for recursion
          canCheckout = checkCanCheckoutFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {
          // If it is a document type of custom interface
          try {
            canCheckout = checkDocument(iResource);

          } catch (Exception ev) {

            // Show the exception if there is one
            logger.error("Exception check", ev);
          }
        }
      }
    }
    return canCheckout;
  }

  /**
   * Checks if a the checkout action is available for current user
   * 
   * @param canCancel
   * @param iResource
   * @return
   */
  private boolean checkDocument(IResource iResource) {
    DocumentImpl doc;
    boolean canUserCheckout = false;

    // If it is a document type of custom interface
    try {
      doc = ((DocumentImpl) iResource);

      // Check if it's not checkout out
      if (!(doc.isCheckedOut() || doc.isPrivateWorkingCopy())) {

        // Allow cancelCheckout
        canUserCheckout = doc.canUserCheckout();
      }

    } catch (

    Exception ev) {

      // Show the exception if there is one
      logger.error("Exception check", ev);
    }
    return canUserCheckout;
  }
}
