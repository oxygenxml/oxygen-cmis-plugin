package com.oxygenxml.cmis.plugin;

import ro.sync.exml.plugin.Plugin;
import ro.sync.exml.plugin.PluginDescriptor;

/**
 * Sample plugin for customs protocols. 
 */
public class CMISPlugin extends Plugin {
  /**
   * The static plugin instance.
   */
  private static CMISPlugin instance = null;

  /**
   * Constructs the plugin.
   * 
   * @param descriptor The plugin descriptor
   */
  public CMISPlugin(PluginDescriptor descriptor) {
    super(descriptor);

    if (instance != null) {
      throw new IllegalStateException("Already instantiated!");
    }
    instance = this;
  }
  
  /**
   * Get the plugin instance.
   * 
   * @return the shared plugin instance.
   */
  public static CMISPlugin getInstance() {
    return instance;
  }
}