package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.plugin.TranslationResourceController;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes the delete folder action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class DeleteFolderAction extends AbstractAction {

  private final String unknownException;

  // Internal role
  private static final String SEARCH_RESULTS_ID = "#search.results";
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
  public DeleteFolderAction(IResource resource, IResource currentParent, ResourcesBrowser itemsPresenter) {
    // Set a name
    super(TranslationResourceController.getMessage("DELETE_FOLDER_ACTION_TITLE"));
    unknownException = TranslationResourceController.getMessage("UNKNOWN_EXCEPTION");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the folder.
   * 
   * <b>This action will delete everything inside the folder (folders,
   * documents)</b>
   * 
   * @param e
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    final PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
    JFrame mainFrame = (JFrame) pluginWorkspace.getParentFrame();
    // Cast to the custom interface to use it's methods
    FolderImpl folderToDelete = ((FolderImpl) resource);

    // Try deleting the folder
    try {
      CMISAccess.getInstance().createResourceController().deleteFolderTree(folderToDelete.getFolder());

      // Present the newly updated content of the parent folder
      if (currentParent.getId().equals(SEARCH_RESULTS_ID)) {
        currentParent.refresh();

      } else {
        currentParent.refresh();
        itemsPresenter.presentResources(currentParent);
      }

    } catch (Exception ev) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(mainFrame, unknownException + ev.getMessage());
    }

  }
}
