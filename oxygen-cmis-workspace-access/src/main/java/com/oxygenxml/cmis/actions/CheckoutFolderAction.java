package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

public class CheckoutFolderAction extends AbstractAction {
  private IResource resource = null;

  public CheckoutFolderAction(IResource resource) {

    super("Check out");
    this.resource = resource;

  }

  @Override
  public void actionPerformed(ActionEvent e) {

    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    if (childrenIterator != null) {

      // While has a child, add to the model
      while (childrenIterator.hasNext()) {

        IResource iResource = childrenIterator.next();

        if (iResource instanceof FolderImpl) {

          checkoutFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {
          try {
            ((DocumentImpl) iResource).checkOut(((DocumentImpl) iResource).getDocType());
          } catch (Exception ev) {
            JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
          }
        }

      }
    }
  }

  private void checkoutFolder(IResource resource) {

    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();
    if (childrenIterator != null) {

      // While has a child, add to the model
      while (childrenIterator.hasNext()) {

        IResource iResource = (IResource) childrenIterator.next();

        if (iResource instanceof FolderImpl) {

          checkoutFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {
          try {
            ((DocumentImpl) iResource).checkOut(((DocumentImpl) iResource).getDocType());
          } catch (Exception ev) {
            JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
          }
        }
      }
    }
  }

}
