package com.oxygenxml.cmis.ui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Custom exception thrown by cancel button
 * 
 * @author bluecc
 *
 */
public class UserCanceledException extends Exception {
  private transient PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

  // Just a message
  public UserCanceledException() {

    // Show an exception if there is one
    JOptionPane.showMessageDialog((JFrame) pluginWorkspace.getParentFrame(), "Exception1 user canceled");
  }
}
