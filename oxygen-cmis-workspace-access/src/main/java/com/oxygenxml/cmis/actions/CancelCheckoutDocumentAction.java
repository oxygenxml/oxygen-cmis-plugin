package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;

public class CancelCheckoutDocumentAction extends AbstractAction {
  IResource resource = null;

  public CancelCheckoutDocumentAction(String name, IResource resource) {
    super(name);

    this.resource = resource;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    DocumentImpl doc = ((DocumentImpl) resource);
    try {

      doc.cancelCheckOut(doc.getDoc());
    } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException ev) {
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }
}
