package com.oxygenxml.cmis.core;

import javax.xml.bind.annotation.XmlRootElement;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

@XmlRootElement(name = "userCredentials")
public class UserCredentials {

  private String username;
  private String password;

  private boolean encrypted = false;

  public UserCredentials() {
  }

  public UserCredentials(String username, String encryptedPassword) {
    this.username = username;
    this.password = encryptedPassword;
  }

  public UserCredentials(String username, String password, boolean encrypt) {
    this.username = username;

    if (encrypt) {
      this.password = PluginWorkspaceProvider.getPluginWorkspace().getUtilAccess().encrypt(password);
      this.encrypted = true;
    } else {
      this.password = password;
    }
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setPassword(String password, boolean encrypt) {
    if (encrypt) {
      this.password = PluginWorkspaceProvider.getPluginWorkspace().getUtilAccess().encrypt(password);
      this.encrypted = true;
    } else {
      this.password = password;
      this.encrypted = false;
    }
  }

  public String getPassword() {
    if (encrypted) {
      return PluginWorkspaceProvider.getPluginWorkspace().getUtilAccess().decrypt(password);
    }
    return password;
  }

  public boolean isEmpty() {
    if (username.isEmpty() && password.isEmpty()) {
      return true;
    }
    return !username.isEmpty() && password.isEmpty();
  }

  @Override
  public String toString() {
    return "Username: " + this.username;
  }

}
