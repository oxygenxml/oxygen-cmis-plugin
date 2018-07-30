package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.ObjectId;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;

public class CheckinDocumentAction extends AbstractAction {
 private  IResource resource = null;

  public CheckinDocumentAction(IResource resource) {
    super("Check in");

    this.resource = resource;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ObjectId res = null;
    DocumentImpl doc = ((DocumentImpl) resource);

    try {
      res = (ObjectId) doc.checkIn();
    } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException ev) {
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }
}
