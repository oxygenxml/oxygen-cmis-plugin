package com.oxygenxml.cmis.plugin;

import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class TranslationResourceController {


  public static String getMessage(String key) {
    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
    
    if (pluginWorkspace != null) {
      PluginResourceBundle resourceBundle = ((StandalonePluginWorkspace) pluginWorkspace).getResourceBundle();
      
      if (resourceBundle != null) {
        return resourceBundle.getMessage(key);
      }
    }
    
    return key;
  }

}
