package com.oxygenxml.cmis.storage;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes the option that Oxygen needs to store. Uses the JAXB serialization
 * creating a XML document and deconstruct
 * 
 * @author bluecc
 *
 */

// Annotation for the root element of the XML document
@XmlRootElement(name = "options")
public class Options {

  /*
   * PluginWorkspace that is used to encrypt and decrypt data
   */
  private final PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
  /**
   * All the known CMIS servers URLs.
   */
  private LinkedHashSet<String> servers;

  /**
   * All the credentials requested from the user.
   */
  @XmlElementWrapper(name = "credentials")
  private HashMap<String, UserCredentials> credentials;
  
  /**
   * Filtering criteria for the search match.
   */
  private String filteringCriteria;
  
  /**
   * @param filteringCriteria Filtering criteria for the search match. One of {@link SearchScopeConstants}.
   */
  public void setFilteringCriteria(String filteringCriteria) {
    this.filteringCriteria = filteringCriteria;
  }
  
  /**
   * @return Filtering criteria for the search match. One of {@link SearchScopeConstants}.
   */
  public String getFilteringCriteria() {
    return filteringCriteria;
  }

  /**
   * @return Gets the CMIS servers URLs.
   */
  @XmlElementWrapper(name = "servers")
  public LinkedHashSet<String> getServers() {
    return servers;
  }

  /*
   * JAXB needs setters and getters for marshal and unmarshal
   */

  public void setServers(LinkedHashSet<String> servers) {
    this.servers = servers;
  }

  public void setCredentials(HashMap<String, UserCredentials> credentials) {
    this.credentials = credentials;
  }

  /**
   * Add server URL to the set
   * 
   * @param currentServerURL
   */
  public void addServer(String currentServerURL) {
    if (this.servers == null) {
      this.servers = new LinkedHashSet<>();
    }

    // Add the URL the servers set
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
   * @see com.oxygenxnl.cmis.core.UserCredentials
   * 
   * @return void
   */
  public void addUserCredentials(String serverURL, UserCredentials uc) {

    // Check if the credentials are not null
    if (credentials == null) {
      credentials = new HashMap<>();
    }

    // Encrypt the password
    String encryptedPass = pluginWorkspace.getUtilAccess().encrypt(uc.getPassword());

    // Add new credentials to the hashmap by creating a new object and put the
    // encrypted password
    credentials.put(serverURL, new UserCredentials(uc.getUsername(), encryptedPass));
  }

  /*
   * Get the decrypted data using the serverURL
   * 
   * @param serverURL
   * 
   * @see com.oxygenxnl.cmis.core.UserCredentials
   * 
   * @return UserCredentials
   */
  public UserCredentials getUserCredentials(URL serverURL) {
    if (credentials != null) {

      // Get the UserCredentials
      UserCredentials uc = credentials.get(serverURL.toExternalForm());

      if (uc != null) {

        // Decrypt the password
        String decryptedPass = pluginWorkspace.getUtilAccess().decrypt(uc.getPassword());

        // Create a new object using the decrypted password
        uc = new UserCredentials(uc.getUsername(), decryptedPass);
      }

      return uc;
    }
    return null;
  }
}
