package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.ObjectId;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.BreadcrumbView;
import com.oxygenxml.cmis.ui.ItemListView;
import com.oxygenxml.cmis.ui.ItemsPresenter;

public class DeleteFolderAction extends AbstractAction {
  private IResource resource;
  private IResource currentParent;
  private ItemsPresenter itemsPresenter;

  public DeleteFolderAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {
    super("Delete");
    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ObjectId res = null;

    FolderImpl folderToDelete = ((FolderImpl) resource);

    try {
      CMISAccess.getInstance().createResourceController().deleteFolderTree(folderToDelete.getFolder());

      itemsPresenter.presentFolderItems(currentParent.getId());

    } catch (Exception ev) {
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }

  }
}
