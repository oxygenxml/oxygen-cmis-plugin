package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.oxygen.cmis.dialogs.DeleteDocDialog;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes the delete document action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class DeleteDocumentAction extends AbstractAction {

  private final transient ResourceController resourceController;
  // The resource to be deleted
  private transient IResource resource;
  // Parent of that resource
  private transient IResource currentParent;
  // Presenter to be able to update the content of the parent
  private transient ResourcesBrowser itemsPresenter;

  /**
   * Constructor that gets the resource to be deleted , currentParent and the
   * presenter to be able to show the updated content of it
   * 
   * @param resource
   * @param currentParent
   * @param itemsPresenter
   */
  public DeleteDocumentAction(IResource resource, IResource currentParent, ResourcesBrowser itemsPresenter) {
    // Set a name
    super("Delete");

    this.resourceController = CMISAccess.getInstance().createResourceController();

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

    setEnabled(((DocumentImpl) resource).canUserDelete());
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
    final PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
    final int defaultValueOfResult = -1;
    int result = -1;
    JFrame mainFrame = (JFrame) pluginWorkspace.getParentFrame();
    DeleteDocDialog inputDialog;
    String deleteType;

    // Cast to the custom type of Document
    final DocumentImpl doc = ((DocumentImpl) resource);

    // If status changed result = 0 means cancel and 1 means yes
    // For that a default value in necessarey
    do {
      // Create the input dialog
      inputDialog = new DeleteDocDialog((JFrame) pluginWorkspace.getParentFrame());

      deleteType = inputDialog.getDeleteType();
      result = inputDialog.getResult();

      if (result == 0) {
        break;
      }
    } while (result == defaultValueOfResult);

    if (result == 1) {
      try {

        // Try to delete <Code>deleteOneVersionDocument</Code>
        if (deleteType.equals("SINGLE")) {

          // Commit the deletion
          resourceController.deleteOneVersionDocument(doc.getDoc());

        } else if (deleteType.equals("ALL")) {

          // Try to delete <Code>deleteAllVersionsDocument</Code>
          // Commit the deletion
          resourceController.deleteAllVersionsDocument(doc.getDoc());
        }

        // Present the new content of the parent resource
        if (currentParent.getId().equals("#search.results")) {
          currentParent.refresh();

        } else {
          currentParent.refresh();
          itemsPresenter.presentResources(currentParent);
        }

      } catch (final Exception ev) {

        // Show the exception if there is one
        JOptionPane.showMessageDialog(mainFrame, "Exception " + ev.getMessage());
      }
    }
  }
}
