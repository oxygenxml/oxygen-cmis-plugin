package com.oxygenxml.cmis.core;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "userCredentials")
public class UserCredentials {
  
  public UserCredentials() {
  }

  public UserCredentials(String username, String password) {
    this.username = username;
    this.password = password;
  }
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
  String username;
  String password;
  
  
  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return super.toString();
  }
}
