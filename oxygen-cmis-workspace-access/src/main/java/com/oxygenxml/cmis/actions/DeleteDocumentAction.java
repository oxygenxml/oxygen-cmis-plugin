package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.ui.ItemsPresenter;

/**
 * Describes the delete document action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class DeleteDocumentAction extends AbstractAction {

  // The resource to be deleted
  private IResource resource;
  // Parent of that resource
  private IResource currentParent;
  // Presenter to be able to update the content of the parent
  private ItemsPresenter itemsPresenter;

  /**
   * Constructor that gets the resource to be deleted , currentParent and the
   * presenter to be able to show the updated content of it
   * 
   * @param resource
   * @param currentParent
   * @param itemsPresenter
   */
  public DeleteDocumentAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {
    // Set a name
    super("Delete");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the document
   * 
   * <b>This action will delete one this version of the document</b>
   * 
   * @param e
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    // Cast to the custom type of Document
    DocumentImpl doc = ((DocumentImpl) resource);

    // Try to delete <Code>deleteOneVersionDocument</Code>
    try {

      // Commit the deletion
      CMISAccess.getInstance().createResourceController().deleteOneVersionDocument(doc.getDoc());

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
