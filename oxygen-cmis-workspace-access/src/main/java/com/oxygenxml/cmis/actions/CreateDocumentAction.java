package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.oxygenxml.cmis.core.model.IResource;

public class CreateDocumentAction extends AbstractAction {
  IResource resource = null;

  public CreateDocumentAction(String name ,IResource resource) {
    super(name);

    this.resource = resource;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // TODO Auto-generated method stub

  }

}
