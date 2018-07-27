package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.ObjectId;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.ItemListView;

public class DeleteFolderAction extends AbstractAction {
  IResource resource = null;

  public DeleteFolderAction(String name,IResource resource) {
    super(name);

    this.resource = resource;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ObjectId res = null;

    FolderImpl folder = ((FolderImpl) resource);

    try {
      CMISAccess.getInstance().createResourceController().deleteFolderTree(folder.getFolder());
    } catch (Exception ev) {
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }
}
