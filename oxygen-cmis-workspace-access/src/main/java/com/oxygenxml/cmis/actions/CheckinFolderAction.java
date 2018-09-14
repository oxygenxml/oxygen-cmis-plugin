package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.ItemsPresenter;

/**
 * Describes the cancel checkout action on a folder by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CheckinFolderAction extends AbstractAction {

  // The resource that will receive
  private IResource resource = null;
  private IResource currentParent = null;
  private ItemsPresenter itemsPresenter = null;

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * @param itemsPresenter
   * @param currentParent
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CheckinFolderAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {

    super("Check in");
    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

    if (checkCanCheckinFolder(resource)) {

      this.enabled = true;

    } else {
      this.enabled = false;

    }
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

    checkinFolder(resource);

    if (currentParent.getId().equals("#search.results")) {
      currentParent.refresh();

    } else {
      currentParent.refresh();
      itemsPresenter.presentResources(currentParent);
    }
  }

  /**
   * Helper method to iterate and commit the <Code> checkinFolder</Code> using
   * the recursion till the child has new children
   * 
   * @param resource
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  private void checkinFolder(IResource resource) {

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
          checkinFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {
          // If it's a document try <Code>checkIn</Code>
          try {
            if (((DocumentImpl) iResource).isCheckedOut() && ((DocumentImpl) iResource).isPrivateWorkingCopy()) {
              
              //TODO Cristian This is a bulk check in. I think we get just one commit message from the user.
              ((DocumentImpl) iResource).checkIn(true, "new comment");
            }
          } catch (Exception ev) {

            // Show the exception if there is one
            JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
          }

        }
      }
    }
  }


  private boolean checkCanCheckinFolder(IResource resource) {
    boolean checkStatus = false;
    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    if (childrenIterator != null) {

      // While has a child, add to the model
      while (childrenIterator.hasNext() && !checkStatus) {

        // Get the next child
        IResource iResource = childrenIterator.next();

        // Check if it's a custom type interface FolderImpl
        if (iResource instanceof FolderImpl) {

          // Call the helper method used for recursion
          checkStatus = checkStatus || checkCanCheckinFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {
          System.out.println("Trying to verify a document name=" + ((DocumentImpl) iResource).getDisplayName());
          // If it is a document type of custom interface
          try {

            if (((DocumentImpl) iResource).isCheckedOut()) {
              // return true if a document was found checked out so
              checkStatus = true;

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
