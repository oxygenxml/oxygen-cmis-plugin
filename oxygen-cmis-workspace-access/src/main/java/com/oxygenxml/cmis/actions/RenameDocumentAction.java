package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

/**
 * Describes how a document is renamed by using the user input from
 * showInputDialog
 * 
 * @author bluecc
 *
 */
public class RenameDocumentAction extends AbstractAction {
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(RenameDocumentAction.class);
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
  public RenameDocumentAction(IResource resource, IResource currentParent, ResourcesBrowser itemsPresenter) {
    super("Rename");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
    
    if (((DocumentImpl) resource).canUserCheckout()) {
      this.enabled = true;
      
    } else {
      this.enabled = false;
    }

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // Cast to the custom type of Document
    DocumentImpl doc = ((DocumentImpl) resource);

    // Get input from user
    String getInput = JOptionPane.showInputDialog(null, "Plase enter a name", "myfile");
    // Try to rename
    try {

      // Commit the deletion
      doc.rename(getInput);

      // Present the new content of the parent resource
      if (currentParent.getId().equals("#search.results")) {
        currentParent.refresh();

      } else {
        currentParent.refresh();
        itemsPresenter.presentResources(currentParent);
      }

    } catch (Exception ev) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }

  }

}
