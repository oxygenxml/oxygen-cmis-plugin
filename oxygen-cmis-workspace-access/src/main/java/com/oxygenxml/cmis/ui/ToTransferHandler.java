package com.oxygenxml.cmis.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

/**
 * Handler that realizes the Drag and Drop actions; Lifecycle:
 * createTransferable() -> importData() -> canImport() -> exportDone(); Default 
 * it is still MOVE use <code>TransferHandler.COPY_OR_MOVE</code>
 * 
 * @author bluecc
 *
 */
class ToTransferHandler extends TransferHandler {
  private int action;
  private int oldIndex = 0;
  protected int dropIndex;
  private JList<IResource> dragList;

  // Initialize the action given
  public ToTransferHandler(JList<IResource> resourceList, int action) {
    //System.out.println("Transfer constructor");
    this.dragList = resourceList;
    this.action = action;
  }

  // Copy or move action
  public int getSourceActions(JComponent comp) {
    return COPY_OR_MOVE;
  }

  // Get the index value or return null in not in the range
  public Transferable createTransferable(JComponent comp) {
    System.out.println("Oldex index=" + oldIndex);
    System.out.println("Size model=" + dragList.getModel().getSize());
    oldIndex = dragList.getSelectedIndex();

    if (oldIndex < 0 || oldIndex > dragList.getModel().getSize()) {
      return null;
    }

    return new StringSelection(dragList.getSelectedValue().getId());
  }

  // Remove the element if the action was a move
  public void exportDone(JComponent comp, Transferable trans, int action) {
    if (action != MOVE) {
      //System.out.println("Not move");
      return;
    }
    //System.out.println("Export done");
    // System.out.println("Index=" + oldIndex);
    // System.out.println("Element=" + from.getElementAt(oldIndex));
    // from.removeElementAt(oldIndex);
    // System.out.println("Index=" + oldIndex);
    // System.out.println("Element=" + from.getElementAt(oldIndex));
  }

  // Check when we can import
  public boolean canImport(TransferHandler.TransferSupport support) {
    // for the demo, we'll only support drops (not clipboard paste)
    if (!support.isDrop()) {
      return false;
    }

    // we only import Strings
    if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      return false;
    }
    // Check if action is supported
    boolean actionSupported = (action & support.getSourceDropActions()) == action;
    if (actionSupported) {
      support.setDropAction(action);
      //System.out.println("Action done=" + action);
      return true;
    }

    return false;
  }

  public boolean importData(TransferHandler.TransferSupport support) {
    // if we can't handle the import, say so
    if (!canImport(support)) {
      //System.out.println("Can import?");
      return false;
    }
    // Get the drop location
    JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
    // Get the model
    DefaultListModel<IResource> listModel = (DefaultListModel<IResource>) dragList.getModel();

    dropIndex = dl.getIndex();
    boolean insert = dl.isInsert();

    // Get the current string under the drop.
    String valueName = ((IResource) listModel.getElementAt(dropIndex)).getDisplayName();

    //System.out.println("Value from import =" + valueName);

    // Get the string that is being dropped.
    Transferable t = support.getTransferable();
    IResource dataFound = null;

    String idData;
    // Get the id
    try {

      idData = (String) t.getTransferData(DataFlavor.stringFlavor);

    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Exception=" + e.getMessage());
      return false;
    }

    // Find the object with that id
    for (int indexModel = 0; indexModel < listModel.size(); indexModel++) {

      IResource elementModel = listModel.getElementAt(indexModel);

      if (idData.equals(elementModel.getId())) {
        dataFound = elementModel;
      }
    }

    // Display a dialog with the drop information.
    String dropValue = "\"" + dataFound.getDisplayName() + "\" dropped ";

    // If an insert happened
    if (insert) {
      // At the beginning of the list
      if (dropIndex == 0) {

        displayDropLocation(dropValue + "at beginning of list");

      } else if (dropIndex >= dragList.getModel().getSize()) {

        displayDropLocation(dropValue + "at end of list");

      } else {
        // Between elements
        String value1 = ((IResource) dragList.getModel().getElementAt(dropIndex - 1)).getDisplayName();
        String value2 = ((IResource) dragList.getModel().getElementAt(dropIndex)).getDisplayName();
        displayDropLocation(dropValue + "between \"" + value1 + "\" and \"" + value2 + "\"");

      }
    } else {
      // On the top of someone
      displayDropLocation(dropValue + "on top of " + "\"" + valueName + "\"");
    }

    // Go into a folder on hover of the drag
    if (((IResource) listModel.getElementAt(dropIndex)) instanceof FolderImpl) {
      // TODO: present that folder
    }

    // If it's a location to drop
    if (insert) {
      //System.out.println("Old index=" + oldIndex);
      //System.out.println("Drop index=" + dropIndex);

      // If is lower then it does destroy the original order
      if (dropIndex < oldIndex) {
        listModel.remove(oldIndex);
        listModel.add(dropIndex, dataFound);
      } else {
        listModel.add(dropIndex, dataFound);
        listModel.remove(oldIndex);
      }

    } else {
      // If on the top of somebody
      listModel.set(dropIndex, dataFound);
    }
    return true;
  }

  // Display the location
  private void displayDropLocation(final String string) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog(null, string);
      }
    });
  }
}