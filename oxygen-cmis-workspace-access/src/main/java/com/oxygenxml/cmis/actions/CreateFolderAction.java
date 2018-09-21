package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes the create folder action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CreateFolderAction extends AbstractAction {
  private static final ResourceController resourceController = CMISAccess.getInstance().createResourceController();
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(CreateFolderAction.class);
  // Presenter of the items
  private transient ResourcesBrowser itemsPresenter;
  // Parent folder where new folder will be created
  private transient IResource currentParent;

  /**
   * Constructor that gets the parent where new folder will be created and a
   * presenter to know what to present
   * 
   * @param currentParent
   * @param itemsPresenter
   */
  public CreateFolderAction(IResource currentParent, ResourcesBrowser itemsPresenter) {
    // Give a name and a native icon
    super("Create Folder", UIManager.getIcon("FileView.directoryIcon"));

    // Set logger level
    

    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the folder
   * 
   * @param e
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    // Plugin workspace
    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
    // Main frame
    JFrame mainFrame = (JFrame) pluginWorkspace.getParentFrame();

    // Get input from user
    String getInput = JOptionPane.showInputDialog(null, "Plase enter a name", "myfolder");
    logger.debug("The input=" + getInput);

    // Set current folder where we want a new folder
    logger.debug("Current parrent=" + currentParent.getDisplayName());
    FolderImpl currentFolder = (FolderImpl) currentParent;

    // Try creating the folder in the currentParent using the input
    try {
      resourceController.createFolder(((FolderImpl) currentParent).getFolder(), getInput);

    } catch (Exception e1) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(mainFrame, "Exception " + e1.getMessage());

    }

    // Present the updated content of the current folder
    itemsPresenter.presentResources(currentFolder.getId());
  }
}
