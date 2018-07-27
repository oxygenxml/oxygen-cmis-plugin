package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;

public class DeleteDocumentAction extends AbstractAction {
  IResource resource = null;

  public DeleteDocumentAction(String name ,IResource resource) {
    super(name);

    this.resource = resource;
  }


  @Override
  public void actionPerformed(ActionEvent e) {
    DocumentImpl doc = ((DocumentImpl) resource);

    try {
      CMISAccess.getInstance().createResourceController().deleteOneVersionDocument(doc.getDoc());
    } catch (Exception ev) {
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }
}
