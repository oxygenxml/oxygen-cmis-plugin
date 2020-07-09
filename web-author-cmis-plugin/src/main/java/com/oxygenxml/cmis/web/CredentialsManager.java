package com.oxygenxml.cmis.web;

import java.util.Optional;

import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * The credentials manager.
 * 
 * @author cristi_talau
 */
public class CredentialsManager {

  /**
   * System property that stores the CMIS service password.
   */
  private static final String CMIS_SERVICE_PASSWORD_PROP = "cmis.service.password";
  /**
   * System property that stores the CMIS service user.
   */
  private static final String CMIS_SERVICE_USER_PROP = "cmis.service.user";
  /**
   * The CMIS plugin credentials key in session store.
   */
  private static final String WA_CMIS_PLUGIN_CREDENTIALS_KEY = "wa-cmis-plugin-credentials";
  /**
   * Singleton instance.
   */
  public static final CredentialsManager INSTANCE = new CredentialsManager();
  /**
   * The session store.
   */
  private final SessionStore sessionStore;
  /**
   * The service account.
   */
  private final Optional<UserCredentials> serviceAccount;
  
  /**
   * Constructor.
   */
  private CredentialsManager() {
    sessionStore = ((WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace())
        .getSessionStore();
    String cmisUser = System.getProperty(CMIS_SERVICE_USER_PROP);
    String cmisPassword = System.getProperty(CMIS_SERVICE_PASSWORD_PROP);
    if (cmisUser != null && cmisPassword != null) {
      serviceAccount = Optional.of(new UserCredentials(cmisUser, cmisPassword));
    } else {
      serviceAccount = Optional.empty();
    }
  }
  
  /**
   * Return the credentials for the given session.
   * @param sessionId the ID of the session.
   * @return The user credentials.
   */
  public UserCredentials getCredentials(String sessionId) {
    return serviceAccount
        .orElseGet(() -> sessionStore.get(sessionId, WA_CMIS_PLUGIN_CREDENTIALS_KEY));
  }

  /**
   * Set the user credentials for the given session.
   * @param sessionId The ID of the session.
   * @param creds The credentials.
   */
  public void setCredentials(String sessionId, UserCredentials creds) {
    sessionStore.put(sessionId, WA_CMIS_PLUGIN_CREDENTIALS_KEY, creds);
  }
  
  /**
   * Forget the user credentials.
   * @param sessionId The session.
   */
  public void forgetUserCredentials(String sessionId) {
    sessionStore.remove(sessionId, WA_CMIS_PLUGIN_CREDENTIALS_KEY);
  }
}
