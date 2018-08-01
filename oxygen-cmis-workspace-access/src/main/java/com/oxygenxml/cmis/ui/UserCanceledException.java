package com.oxygenxml.cmis.ui;

public class UserCanceledException extends Exception {
  
  //Just a message
  public UserCanceledException() {
    System.out.println("User cancels exception");
  }
}
