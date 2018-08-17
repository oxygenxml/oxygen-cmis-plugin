package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.ItemListView;
import com.oxygenxml.cmis.ui.ItemsPresenter;

/**
 * Describes the cancel checkout action on a folder by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CheckoutFolderAction extends AbstractAction {

  // The resource that will receive
  private IResource resource = null;
  private IResource currentParent = null;
  private ItemsPresenter itemsPresenter = null;

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * @param itemListView
   * @param currentParent
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CheckoutFolderAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {

    super("Check out");
    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

    if (checkCanCheckoutFolder(resource)) {

      this.enabled = true;

    } else {
      this.enabled = false;

    }
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
    if (currentParent.getId().equals("#search.results")) {
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
        IResource iResource = (IResource) childrenIterator.next();

        // Check if it's an instance of custom interface Folder
        if (iResource instanceof FolderImpl) {

          // Call recursively if it's a folder
          checkoutFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {
          // Try <Code>checkOut</Code> if it's an instance of a custom interface
          // of Document
          try {
            // Commit the checkOut
            boolean checkedOutStatus = ((DocumentImpl) iResource).isCheckedOut();
            boolean privateCopyStatus = ((DocumentImpl) iResource).isPrivateWorkingCopy();

            if (!(checkedOutStatus || privateCopyStatus)) {
              ((DocumentImpl) iResource).checkOut(((DocumentImpl) iResource).getDocType());
            }
          } catch (Exception ev) {

            // Show there exception if there is one
            JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
          }
        }
      }
    }
  }

  private boolean checkCanCheckoutFolder(IResource resource) {
    boolean checkStatus = false;
    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    if (childrenIterator != null) {

      // While has a child, add to the model
      while (childrenIterator.hasNext() && !checkStatus) {

        // Get the next child
        IResource iResource = (IResource) childrenIterator.next();

        // Check if it's a custom type interface FolderImpl
        if (iResource instanceof FolderImpl) {

          // Call the helper method used for recursion
          checkStatus = checkStatus || checkCanCheckoutFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {
          System.out.println("Trying to verify a document name=" + ((DocumentImpl) iResource).getDisplayName());
          // If it is a document type of custom interface
          try {
            boolean checkedOutStatus = ((DocumentImpl) iResource).isCheckedOut();
            boolean privateCopyStatus = ((DocumentImpl) iResource).isPrivateWorkingCopy();

            if (!(checkedOutStatus || privateCopyStatus)) {
              // return true if a document was found checked out so
              return true;

            }

          } catch (Exception ev) {

            // Show the exception if there is one
            JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
          }
        }
      }
    }
    return checkStatus;
  }
}
