package com.oxygenxml.cmis.core;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

/**
 * Entry point to access a CMIS server.
 */
public class CMISAccess {
  /**
   * Singleton instance.
   */
  private static CMISAccess instance;
  /**
   * Session factory.
   */
  private SessionFactoryImpl factory;
  /**
   * A session to the CMIS server. As as far as I can tell, this object can be kept and reused:
   * 
   * CMIS itself is state-less. OpenCMIS uses the concept of a session to cache
   * data across calls and to deal with user authentication. 
   */
  private Session session;


  /**
   * Private constructor.
   */
  private CMISAccess() {
    factory = SessionFactoryImpl.newInstance();
  }

  public static CMISAccess getInstance() {
    if (instance == null) {
      instance = new CMISAccess();
    }
    return instance;
  }

  /**
   * "http://localhost:8080/atom11"
   * 
   * Creates a connection to the given server.
   * 
   * @param connectionInfo Server location.
   * @param repositoryID Repository ID.
   */
  public void connect(URL connectionInfo, String repositoryID) {
    HashMap<String, String> parameters = new HashMap<>();
    populateParameters(connectionInfo, parameters);
    parameters.put(SessionParameter.REPOSITORY_ID, repositoryID);

    // create session
    session = factory.createSession(parameters);
  }

  private void populateParameters(URL connectionInfo, HashMap<String, String> parameters) {
    // TODO Ask for credentials. Different implementations SA/Web
    parameters.put(SessionParameter.USER, "admin");
    parameters.put(SessionParameter.PASSWORD, "admin");

    // connection settings
    parameters.put(SessionParameter.ATOMPUB_URL, connectionInfo.toString());
    parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
    
    parameters.put(SessionParameter.HTTP_INVOKER_CLASS, "org.apache.chemistry.opencmis.client.bindings.spi.http.ApacheClientHttpInvoker");
  }
  
  /**
   * TODO Alexey Make some tests.
   * 
   * Gets the available repositories in the server.
   * 
   * @param connectionInfo
   * @return
   */
  public List<Repository> getRepositories(URL connectionInfo) {
    HashMap<String, String> parameters = new HashMap<>();
    populateParameters(connectionInfo, parameters);

    return factory.getRepositories(parameters);
  }
  
  /**
   * @return A controller to work with resources.
   */
  public ResourceController createResourceController() {
    return new ResourceController(session);
  }

}
