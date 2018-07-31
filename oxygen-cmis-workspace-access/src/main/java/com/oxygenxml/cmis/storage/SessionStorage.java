package com.oxygenxml.cmis.storage;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

/**
 * Singleton
 * 
 * SERVERS CREDENTIALS
 * 
 * 
 * 
 * @author bluecc
 *
 */
public class SessionStorage {
  private static SessionStorage instance;
  private PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

  public static SessionStorage getInstance() {
    if (instance == null) {
      instance = new SessionStorage();
    }
    return instance;
  }

  public String[] getSevers() {
    
    // Create the default servers
    String[] serversList = new String[] { 
        "http://127.0.0.1:8098/alfresco/api/-default-/cmis/versions/1.1/atom",
        "http://localhost:8088/alfresco/api/-default-/cmis/versions/1.1/atom" };

    String[] storage = null;

    storage = pluginWorkspace.getOptionsStorage().getStringArrayOption("servers", serversList);

    return storage;
  }

  public void setServers(String[] servers) {
    pluginWorkspace.getOptionsStorage().setStringArrayOption("servers", servers);
  }

}
