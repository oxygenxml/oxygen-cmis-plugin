package com.oxygenxml.cmis.ui;

import javax.swing.JOptionPane;

/**
 * Custom exception thrown by cancel button
 * 
 * @author bluecc
 *
 */
public class UserCanceledException extends Exception {

  // Just a message
  public UserCanceledException() {

    // Show an exception if there is one
    JOptionPane.showMessageDialog(null, "Exception use canceled");
  }
}
