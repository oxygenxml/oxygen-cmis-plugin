package com.oxygenxml.cmis.actions;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import com.icl.saxon.functions.Current;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.core.urlhandler.CustomProtocolExtension;
import com.oxygenxml.cmis.ui.ItemsPresenter;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CreateDocumentAction extends AbstractAction {
  private IResource resource = null;
  private IResource currentParent;
  private ItemsPresenter itemsPresenter;
  private Document documentCreated;

  public CreateDocumentAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {
    super("Create document");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // Get input from user
    String getInput = JOptionPane.showInputDialog(null, "Plase enter a name", "myfile");
    System.out.println("The input=" + getInput);



    try {
      documentCreated = CMISAccess.getInstance().createResourceController()
          .createVersionedDocument(((FolderImpl) resource).getFolder(), getInput, "", VersioningState.MAJOR);
    } catch (UnsupportedEncodingException e1) {

      JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
      e1.printStackTrace();
    }

    // Open into Oxygen

    String urlAsTring = CustomProtocolExtension.getCustomURL(documentCreated,
        CMISAccess.getInstance().createResourceController());

    System.out.println(urlAsTring);

    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

    if (pluginWorkspace != null) {

      try {
        pluginWorkspace.open(new URL(urlAsTring));
      } catch (MalformedURLException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

    }
    itemsPresenter.presentFolderItems(currentParent.getId());
  }

  // TODO try catch handling
}
