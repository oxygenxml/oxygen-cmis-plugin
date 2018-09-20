package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.oxygenxml.cmis.core.model.IResource;

public class CopyFolderAction extends AbstractAction {
  private IResource resource = null;

  /*
   * TODO COPY EVERYTHING TO CLIPBOARD DOES NOT WORK ANYTHING JUST TO EXIST
   */
  public CopyFolderAction(IResource resource) {
    super("Copy");

    this.resource = resource;
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    //
    // FolderImpl folder = ((FolderImpl) resource);
    //
    // try {
    // CMISAccess.getInstance().createResourceController().deleteFolderTree(folder.getFolder());
    // } catch (Exception ev) {
    // JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    // }
  }
}
