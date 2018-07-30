package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.ItemsPresenter;

public class CreateFolderAction extends AbstractAction {
  private ItemsPresenter itemsPresenter;
  private IResource currentParent;

  public CreateFolderAction(IResource currentParent, ItemsPresenter itemsPresenter) {
    super("Create Folder");
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    // Get input from user
    String getInput = JOptionPane.showInputDialog(null, "Plase enter a name", "myfolder");
    System.out.println("The input=" + getInput);

    // Set current folder
    FolderImpl currentFolder = (FolderImpl) currentParent;

    try {
      CMISAccess.getInstance().createResourceController().createFolder(((FolderImpl) currentParent).getFolder(),
          getInput);
    } catch (Exception e1) {

      JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
      e1.printStackTrace();
    }

    itemsPresenter.presentFolderItems(currentFolder.getId());
  }
}
