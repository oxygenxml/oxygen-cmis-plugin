package com.oxygenxml.cmis.storage;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.oxygenxml.cmis.core.UserCredentials;

// Set the root element
@XmlRootElement(name = "options")
public class Options {
  

  private LinkedHashSet<String> servers;
  
  //Set the wrapper for credentials
  @XmlElementWrapper(name="credentials")
  Map<String, UserCredentials> credentials;
  
  //Set the wrapper for servers
  @XmlElementWrapper(name="servers")
  public LinkedHashSet<String> getServers() {
    return servers;
  }

  public void setServers(LinkedHashSet<String> serversList) {
    this.servers = serversList;
  }

  public Map<String, UserCredentials> getCredentials() {
    return credentials;
  }

  
  // Add the credentials to the hashmap
  public void addUserCredentials(String serverURL, UserCredentials uc) {
    if (credentials == null) {
      credentials = new HashMap<>();
    }
    credentials.put(serverURL, uc);
  }
}
