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
public class CancelCheckoutFolderAction extends AbstractAction {

  // The resource that will receive
  private IResource resource = null;

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CancelCheckoutFolderAction(IResource resource) {

    super("Cancel check out");
    this.resource = resource;

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

    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    // Check if
    if (childrenIterator != null) {

      // While has a child, add to the model
      while (childrenIterator.hasNext()) {

        // Get the next child
        IResource iResource = childrenIterator.next();

        // Check if it an instance of custom interface Folder
        if (iResource instanceof FolderImpl) {

          // Call the helper method used for recursion
          cancelCheckoutFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {

          // If it is a document type of custom interface
          try {

            // Commit the <Code>cancelCheckOout()</Code>
            ((DocumentImpl) iResource).cancelCheckOut();

          } catch (Exception ev) {

            // Show the exception if there is one
            JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
          }
        }

      }
    }
  }

  /**
   * Helper method to iterate and commit the <Code> cancelCheckOut</Code> using
   * the recursion till the child has new children
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

        // Get the next child
        IResource iResource = (IResource) childrenIterator.next();

        // Check if it's a custom type interface FolderImpl
        if (iResource instanceof FolderImpl) {

          // Call the helper method used for recursion
          cancelCheckoutFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {

          // If it is a document type of custom interface
          try {

            // Commit the <Code>cancelCheckOout()</Code>
            ((DocumentImpl) iResource).cancelCheckOut();

          } catch (Exception ev) {

            // Show the exception if there is one
            JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
          }
        }
      }
    }
  }
}
