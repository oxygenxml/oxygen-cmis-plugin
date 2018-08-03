package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

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

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CheckinFolderAction(IResource resource) {

    super("Check in");
    this.resource = resource;

  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the folder using the recursion.
   * 
   * <Code>checkinFolder</Code> will be called whenever a folder child
   * folder will be encountered, otherwise the checkin will precede
   * 
   * @param e
   * @exception org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    // Check if it's not null
    if (childrenIterator != null) {

      // While has a child, add to the model
      while (childrenIterator.hasNext()) {

        // Get the next child
        IResource iResource = childrenIterator.next();

        // Check if it an instance of custom interface Folder
        if (iResource instanceof FolderImpl) {

          // Try calling the <Code>checkinFolder</Code>
          try {
            checkinFolder(iResource);

          } catch (Exception ev) {
            // Show the exception if there is one
            JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
          }

        } else if (iResource instanceof DocumentImpl) {

          // If it's a document try <Code>checkIn</Code>
          try {
            ((DocumentImpl) iResource).checkIn();

          } catch (Exception ev) {

            // Show the exception if there is one
            JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
          }
        }

      }
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
        IResource iResource = (IResource) childrenIterator.next();

        // Check if it an instance of custom interface Folder
        if (iResource instanceof FolderImpl) {

          // Call the checkinFolder again recursively
          checkinFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {
          // If it's a document try <Code>checkIn</Code>
          try {
            ((DocumentImpl) iResource).checkIn();

          } catch (Exception ev) {
            
            // Show the exception if there is one
            JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
          }

        }
      }
    }
  }
}
