package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.Document;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;

public class CheckoutDocumentAction extends AbstractAction {
  IResource resource = null;

  public CheckoutDocumentAction(IResource resource) {
    super("Check out");

    this.resource = resource;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Document res = null;
    DocumentImpl doc = ((DocumentImpl) resource);
    try {
      res = doc.checkOut(doc.getDocType());
      
    } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException ev) {
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }

    System.out.println(res);
  }
}
