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

  /*
   * User credentials shown
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    String result = "Username=" + this.username + " " + "Password=" + this.password;
    return result;
  }

}
