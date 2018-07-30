package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

public class CheckinFolderAction extends AbstractAction {

  private IResource resource = null;

  public CheckinFolderAction(IResource resource) {

    super("Check in");
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
          try {
            checkinFolder(iResource);

          } catch (Exception ev) {
            JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
          }

        } else if (iResource instanceof DocumentImpl) {
          ((DocumentImpl) iResource).checkIn();
        }

      }
    }
  }

  private void checkinFolder(IResource resource) {

    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    if (childrenIterator != null) {

      // While has a child, add to the model
      while (childrenIterator.hasNext()) {

        IResource iResource = (IResource) childrenIterator.next();

        if (iResource instanceof FolderImpl) {

          checkinFolder(iResource);

        } else if (iResource instanceof DocumentImpl) {
          
          try {
            ((DocumentImpl) iResource).checkIn();
          } catch (Exception ev) {
            JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
          }
          
        }
      }
    }
  }
}
