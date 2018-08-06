package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.core.urlhandler.CmisURLExtension;
import com.oxygenxml.cmis.ui.ItemsPresenter;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes the copy action on a document by extending the AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CreateDocumentAction extends AbstractAction {
  // Resource to use for creation of the document
  private IResource resource = null;
  // Parent of the resource
  private IResource currentParent;
  // Presenter to use to show the resources
  private ItemsPresenter itemsPresenter;
  // New document created
  private Document documentCreated;

  /**
   * Constructor that receives data to process for the creation and presentation
   * of the newly created document
   * 
   * @param resource
   * @param currentParent
   * @param itemsPresenter
   */
  public CreateDocumentAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {

    // Set a name and use a native icon
    super("Create document", UIManager.getIcon("FileView.fileIcon"));

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the document
   * 
   * @param e
   * @exception UnsupportedEncodingException
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    // Get input from user
    String getInput = JOptionPane.showInputDialog(null, "Plase enter a name", "myfile");
    System.out.println("The input=" + getInput);

    // Try creating the document with the string from the input
    try {

      // Create a versioned document with the state of MAJOR
      documentCreated = CMISAccess.getInstance().createResourceController()
          .createVersionedDocument(((FolderImpl) resource).getFolder(), getInput, "", VersioningState.MAJOR);

    } catch (UnsupportedEncodingException e1) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
    }

    // -------- Open into Oxygen
    String urlAsTring = null;

    try {
      // Get the <Code>getCustomURL</Code> of the document created
      urlAsTring = CmisURLExtension.getCustomURL(documentCreated,
          CMISAccess.getInstance().createResourceController());

    } catch (UnsupportedEncodingException e2) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + e2.getMessage());
    }

    System.out.println(urlAsTring);

    // Get the workspace of the plugin
    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

    // Check if it's not null
    if (pluginWorkspace != null) {

      // Try openning the url in the workspace
      try {
        pluginWorkspace.open(new URL(urlAsTring));

      } catch (MalformedURLException e1) {

        // Show the exception if there is one
        JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
      }

    }
    // --------
    
    
    // Presenter the updated content of the parent folder
    itemsPresenter.presentFolderItems(currentParent.getId());
  }

}
