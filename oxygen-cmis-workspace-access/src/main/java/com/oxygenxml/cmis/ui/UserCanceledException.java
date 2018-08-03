package com.oxygenxml.cmis.ui;

/**
 * Custom exception thrown by cancel button
 * 
 * @author bluecc
 *
 */
public class UserCanceledException extends Exception {

  // Just a message
  public UserCanceledException() {
    System.out.println("User cancels exception");
  }
}
