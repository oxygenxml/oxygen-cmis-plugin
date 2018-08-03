package com.oxygenxml.cmis.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.Document;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.ItemsPresenter;

/**
 * Describes the delete folder action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class PasteDocumentAction extends AbstractAction {
  // The resource to paste
  private IResource resource;
  // Parent of tht resource
  private IResource currentParent;
  // Presenter to update the content of that parent
  private ItemsPresenter itemsPresenter;

  /**
   * Constructor that gets the resource (where to paste), currentParent of that
   * resource and the presenter of the parent
   * 
   * @param resource
   * @param currentParent
   * @param itemsPresenter
   */
  public PasteDocumentAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {
    // Set a name
    super("Paste document");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
  }

  /**
   * Get the system clipboard text
   * 
   * @return string
   */

  private String getSysClipboardText() {
    // Initialize the returned text
    String ret = null;

    // Get the clipboard
    Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    // Get the transfer object
    Transferable clipTf = sysClip.getContents(null);

    // If there is something
    if (clipTf != null) {

      // TODO Customize dataflavor
      if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        try {

          ret = (String) clipTf.getTransferData(DataFlavor.stringFlavor);

        } catch (Exception e) {

          // Show the exepction if there is one
          JOptionPane.showMessageDialog(null, "Exception " + e.getMessage());
        }
      }
    }

    return ret;
  }

  // Is called every time the class is instantiated
  // Checks if it's enabled and makes sure there is a paste action that is
  // deactivated
  @Override
  public boolean isEnabled() {

    // If there is something in the clipboard
    return super.isEnabled() && getSysClipboardText() != null;
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the clipboard.
   * 
   * <b>This action will delete everything inside the folder (folders,
   * documents)</b>
   * 
   * @param e
   * @exception MalformedURLException
   *              , UnsupportedEncodingException
   * 
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    // Checks if there is something in the clipboard
    if (getSysClipboardText() != null) {

      // Try to get the document
      try {
        // Get the document
        Document docClipboard = CMISAccess.getInstance().createResourceController().getDocument(getSysClipboardText());

        // Add the document from clipboard to the currentFolder
        CMISAccess.getInstance().createResourceController().addToFolder(((FolderImpl) resource).getFolder(),
            docClipboard);

        // Presenter the new content of the current parent
        itemsPresenter.presentFolderItems(currentParent.getId());

      } catch (Exception ev) {
        
        // Show the exception if there is one
        JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
      }
    }
  }

}