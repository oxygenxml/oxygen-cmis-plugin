package com.oxygenxml.cmis.storage;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.oxygenxml.cmis.core.UserCredentials;

/**
 * A collections with everything that needs remembering. 
 */
@XmlRootElement(name = "options")
public class Options {
  
  /**
   * All the known CMIS servers URLs.  
   */
  private LinkedHashSet<String> servers;
  
  /**
   * All the credentials requested from the user.
   */
  @XmlElementWrapper(name="credentials")
  Map<String, UserCredentials> credentials;
  
  /**
   * @return Gets the CMIS servers URLs.
   */
  @XmlElementWrapper(name="servers")
  public Set<String> getServers() {
    return servers;
  }

  public void addServer(String currentServerURL) {
    if (this.servers == null) {
      this.servers = new LinkedHashSet<>();
    }
    this.servers.add(currentServerURL);
  }

  
  // Add the credentials to the hashmap
  public void addUserCredentials(String serverURL, UserCredentials uc) {
    if (credentials == null) {
      credentials = new HashMap<>();
    }
    // TODO Encrypt before adding.
    credentials.put(serverURL, uc);
  }

  public UserCredentials getUserCredentials(URL serverURL) {
    if (credentials != null) {
      // TODO Decrypt before returning.
      return credentials.get(serverURL.toExternalForm());
    }
    return null;
  }
}
