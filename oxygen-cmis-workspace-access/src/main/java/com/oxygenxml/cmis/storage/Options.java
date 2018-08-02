package com.oxygenxml.cmis.storage;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * A collections with everything that needs remembering.
 */
@XmlRootElement(name = "options")
public class Options {

  /*
   * PluginWorkspace that is used to encrypt and decrypt data
   */
  private PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
  private String encryptedPass;
  private String decryptedPass;
  /**
   * All the known CMIS servers URLs.
   */
  private LinkedHashSet<String> servers;

  /**
   * All the credentials requested from the user.
   */
  @XmlElementWrapper(name = "credentials")
  Map<String, UserCredentials> credentials;

  /**
   * @return Gets the CMIS servers URLs.
   */
  @XmlElementWrapper(name = "servers")
  public LinkedHashSet<String> getServers() {
    return servers;
  }

  public void addServer(String currentServerURL) {
    if (this.servers == null) {
      this.servers = new LinkedHashSet<>();
    }
    this.servers.add(currentServerURL);
  }

  /*
   * Use the serverURL and user credentials to ecrypt the data and put it to
   * credentials map
   * 
   * @param serverURL
   * 
   * @param uc
   * 
   * @return void
   */
  public void addUserCredentials(String serverURL, UserCredentials uc) {

    // Check if the credentials are not null
    if (credentials == null) {
      credentials = new HashMap<>();
    }

    // Encrypt the password
    encryptedPass = pluginWorkspace.getUtilAccess().encrypt(uc.getPassword());

    // Set the encrypted password
    uc.setPassword(encryptedPass);

    credentials.put(serverURL, uc);
  }

  /*
   * Get the decrypted data using the serverURL
   * 
   * @param serverURL
   * 
   * @return UserCredentials
   */
  public UserCredentials getUserCredentials(URL serverURL) {
    if (credentials != null) {

      // Get the UserCredentials
      UserCredentials uc = credentials.get(serverURL.toExternalForm());

      // Decrypt the password
      decryptedPass = pluginWorkspace.getUtilAccess().decrypt(uc.getPassword());

      // Set the real password and return
      uc.setPassword(decryptedPass);

      return uc;
    }
    return null;
  }
}
