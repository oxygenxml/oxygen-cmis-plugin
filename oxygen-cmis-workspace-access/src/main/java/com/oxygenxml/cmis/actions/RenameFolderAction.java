package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes how a folder is renamed by using the user input from
 * showInputDialog
 * 
 * @author bluecc
 *
 */
public class RenameFolderAction extends AbstractAction {
  private static transient PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
  private static JFrame mainFrame = (JFrame) pluginWorkspace.getParentFrame();
  

  private transient IResource resource = null;
  private  transient IResource currentParent = null;
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
  public RenameFolderAction(IResource resource, IResource currentParent, ResourcesBrowser itemsPresenter) {
    super("Rename");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // Cast to the custom type of Document
    FolderImpl folder = ((FolderImpl) resource);

    // Get input from user
    String getInput = JOptionPane.showInputDialog(mainFrame, "Plase enter a name", resource.getDisplayName());
 
    // Try to rename
    try {

      // Commit the deletion
      folder.rename(getInput);

      // Present the new content of the parent resource
      if (currentParent.getId().equals("#search.results")) {
        currentParent.refresh();

      } else {
        currentParent.refresh();
        itemsPresenter.presentResources(currentParent);
      }

    } catch (Exception ev) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(mainFrame, "Exception " + ev.getMessage());
    }

  }

}
