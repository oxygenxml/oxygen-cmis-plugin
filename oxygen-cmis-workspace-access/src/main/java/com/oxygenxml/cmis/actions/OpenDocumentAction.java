package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.urlhandler.CustomProtocolExtension;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class OpenDocumentAction extends AbstractAction {
  IResource resource = null;

  public OpenDocumentAction(String name,IResource resource) {
    super(name);

    this.resource = resource;
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    String urlAsTring = CustomProtocolExtension.getCustomURL(((DocumentImpl) resource).getDoc(),
        CMISAccess.getInstance().createResourceController());

    System.out.println(urlAsTring);

    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

    if (pluginWorkspace != null) {

      try {
        pluginWorkspace.open(new URL(urlAsTring));
      } catch (MalformedURLException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

    }
  }
}
