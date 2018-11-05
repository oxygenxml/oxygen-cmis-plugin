package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.plugin.TranslationResourceController;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes how a document is renamed by using the user input from
 * showInputDialog
 * 
 * @author bluecc
 *
 */
public class RenameDocumentAction extends AbstractAction {
  private final String unknownException;
  private final String enterNameLabel;
  // Internal role
  private static final String SEARCH_RESULTS_ID = "#search.results";
  private static transient PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
  private static JFrame mainFrame = (JFrame) pluginWorkspace.getParentFrame();

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
    super(TranslationResourceController.getMessage("RENAME_ACTION_TITLE"));
    unknownException = TranslationResourceController.getMessage("UNKNOWN_EXCEPTION");
    enterNameLabel = TranslationResourceController.getMessage("ENTER_A_NAME");
    
    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

    setEnabled(((DocumentImpl) resource).canUserUpdateProperties());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // Cast to the custom type of Document
    DocumentImpl doc = ((DocumentImpl) resource);

    // Get input from user
    String getInput = JOptionPane.showInputDialog(mainFrame, enterNameLabel, resource.getDisplayName());
    // Try to rename
    try {

      // Commit the deletion
      doc.rename(getInput);

      // Present the new content of the parent resource
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
