package com.oxygenxml.cmis.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;

public class CopyDocumentAction extends AbstractAction {
  IResource resource = null;

  public CopyDocumentAction(IResource resource) {
    super("Copy");

    this.resource = resource;
  }

  /**
   * put string into Clipboard
   */
  private void setSysClipboardText(String writeMe) {

    Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable tText = new StringSelection(writeMe);
    clip.setContents(tText, null);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    try {
      
      DocumentImpl doc = ((DocumentImpl) resource);
      setSysClipboardText(doc.getId());
      
    } catch (Exception ev) {
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }

}