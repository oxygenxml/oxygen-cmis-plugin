package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.BreadcrumbView;
import com.oxygenxml.cmis.ui.ItemsPresenter;

public class DeleteDocumentAction extends AbstractAction {
  IResource resource;
  IResource currentParent;
  ItemsPresenter itemsPresenter;

  public DeleteDocumentAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {
    super("Delete");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    DocumentImpl doc = ((DocumentImpl) resource);

    try {
      CMISAccess.getInstance().createResourceController().deleteOneVersionDocument(doc.getDoc());
      
      itemsPresenter.presentFolderItems(currentParent.getId());
      
    } catch (Exception ev) {
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }
}
