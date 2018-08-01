package com.oxygenxml.cmis.storage;

import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.oxygenxml.cmis.core.UserCredentials;

@XmlRootElement(name = "options")
public class Options {
  

  private LinkedHashSet<String> servers;
  
  @XmlElementWrapper(name="credentials")
  Map<String, UserCredentials> credentials;
  
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

  
  public void addUserCredentials(String serverURL, UserCredentials uc) {
    if (credentials == null) {
      credentials = new HashMap<>();
    }
    credentials.put(serverURL, uc);
  }
}
