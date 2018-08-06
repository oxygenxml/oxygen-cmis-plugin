package com.oxygenxml.cmis.core;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

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
   * A session to the CMIS server. As as far as I can tell, this object can be
   * kept and reused:
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

  public void connectToRepo(URL connectionInfo, String repositoryID) throws CmisUnauthorizedException {
    connectToRepo(connectionInfo, repositoryID, null);
  }

  /**
   * "http://localhost:8080/atom11 http://localhost:8080/B/atom11"
   * 
   * Creates a connection to the given server.
   * 
   * @param connectionInfo
   *          Server location.
   * @param repositoryID
   *          Repository ID.
   */
  public void connectToRepo(URL connectionInfo, String repositoryID, UserCredentials uc)
      throws CmisUnauthorizedException {
    HashMap<String, String> parameters = new HashMap<>();
    System.out.println("Before try to connect to repo");
    populateParameters(connectionInfo, parameters, uc);
    parameters.put(SessionParameter.REPOSITORY_ID, repositoryID);

    // create session
    session = factory.createSession(parameters);
    System.out.println("After try to connect to repo");
  }

  private void populateParameters(URL connectionInfo, HashMap<String, String> parameters, UserCredentials uc) {
    // TODO Ask for credentials. Different implementations SA/Web
    if (uc != null) {
      parameters.put(SessionParameter.USER, uc.username);
      parameters.put(SessionParameter.PASSWORD, String.valueOf(uc.password));
    } else {
      parameters.put(SessionParameter.USER, "admin");
      parameters.put(SessionParameter.PASSWORD, "admin");
    }

    // connection settings
    parameters.put(SessionParameter.ATOMPUB_URL, connectionInfo.toString());
    parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

    parameters.put(SessionParameter.HTTP_INVOKER_CLASS,
        "org.apache.chemistry.opencmis.client.bindings.spi.http.ApacheClientHttpInvoker");
  }

  /**
   * Gets the available repositories in the server.
   * 
   * @param connectionInfo
   * @return
   */
  public List<Repository> connectToServerGetRepositories(URL connectionInfo, UserCredentials uc) {
    HashMap<String, String> parameters = new HashMap<>();
    populateParameters(connectionInfo, parameters, uc);

    return factory.getRepositories(parameters);
  }

  /**
   * @return A controller to work with resources.
   */
  public ResourceController createResourceController() {
    return new ResourceController(session);
  }

  public Session getSession() {
    return session;
  }

}
