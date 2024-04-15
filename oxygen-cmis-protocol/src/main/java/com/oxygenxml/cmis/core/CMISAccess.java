package com.oxygenxml.cmis.core;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private Map<String, String> parameters;

	/**
	 * Private constructor. For Web-Author we need more instances of CMISAccess
	 */
	public CMISAccess() {
		factory = SessionFactoryImpl.newInstance();
	}

	public CMISAccess clone() {
		CMISAccess cmisAccess = new CMISAccess();

		cmisAccess.parameters = parameters;
		// create session
		cmisAccess.session = factory.createSession(parameters);
		return cmisAccess;
	}

	/**
	 * "http://localhost:8080/atom11 http://localhost:8080/B/atom11"
	 * 
	 * Creates a connection to the given server.
	 * 
	 * @param connectionInfo Server location.
	 * @param repositoryID   Repository ID.
	 *            
	 * @exception CmisUnauthorizedException Bad credentials.
	 */
	public void connectToRepo(URL connectionInfo, String repositoryID, CmisCredentials uc)
			throws CmisUnauthorizedException {
		parameters = new HashMap<>();

		populateParameters(connectionInfo, parameters, uc);
		parameters.put(SessionParameter.REPOSITORY_ID, repositoryID);

		// create session
		session = factory.createSession(parameters);
	}

  private void populateParameters(URL connectionInfo, Map<String, String> parameters, CmisCredentials uc) {
    if (uc != null) {
      if (uc instanceof UserCredentials) {
        UserCredentials creds = (UserCredentials) uc;
        parameters.put(SessionParameter.USER, creds.getUsername());
        parameters.put(SessionParameter.PASSWORD, String.valueOf(creds.getPassword()));  
      } else if (uc instanceof TokenCredentials) {
        TokenCredentials creds = (TokenCredentials) uc;
        String token = creds.getToken();
        parameters.put(SessionParameter.USER, "ROLE_TICKET");
        parameters.put(SessionParameter.PASSWORD, token);
      }
    }
    

		// connection settings
		parameters.put(SessionParameter.ATOMPUB_URL, connectionInfo.toString());
		parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameters.put(SessionParameter.CONNECT_TIMEOUT, "5000");
    parameters.put(SessionParameter.READ_TIMEOUT, "5000");
    
    parameters.put(SessionParameter.LATEST_VERSION_WITH_VERSION_SERIES_ID, "true");
    parameters.put(SessionParameter.ADD_NAME_ON_CHECK_IN, "true");
    parameters.put(SessionParameter.OMIT_CHANGE_TOKENS, "true");
    
		parameters.put(SessionParameter.HTTP_INVOKER_CLASS,
		    OxygenHttpInvoker.class.getName());
	}

	/**
	 * Gets the available repositories in the server.
	 * 
	 * @param connectionInfo
	 * @return List<Repository>
	 * 
	 * @throws CmisUnauthorizedException bad credentials.
	 */
	public List<Repository> connectToServerGetRepositories(URL connectionInfo, CmisCredentials uc) throws CmisUnauthorizedException {
		HashMap<String, String> conParameters = new HashMap<>();
		populateParameters(connectionInfo, conParameters, uc);

		return factory.getRepositories(conParameters);
	}
	
	/**
	 * 
	 * @param connectionInfo
	 * @param uc
	 */
	public void pureConnectToServer(URL connectionInfo, CmisCredentials uc) {
		HashMap<String, String> conParameters = new HashMap<>();
		populateParameters(connectionInfo, conParameters, uc);
		
		String repositoryID = factory.getRepositories(conParameters).get(0).getId();
		conParameters.put(SessionParameter.REPOSITORY_ID, repositoryID);
		
		session = factory.createSession(conParameters);
	}
	
	/**
	 * @return A controller to work with resources.
	 */
	public ResourceController createResourceController() {
	  if (session == null) {
	    throw new IllegalAccessError("Not connected to reposiotry. This controller can't be used.");
	  }
		return new ResourceController(session);
	}

	public Session getSession() {
		return session;
	}

	public String getCMSProductName() {
	  return session.getRepositoryInfo().getProductName();
	}
	
	public boolean isSharePoint() {
	  String productName = getCMSProductName();
	  return productName != null && productName.toLowerCase().contains("sharepoint");
	}
}
