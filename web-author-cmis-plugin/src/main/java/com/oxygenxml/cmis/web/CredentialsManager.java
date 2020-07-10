package com.oxygenxml.cmis.web;

import java.util.Optional;

import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;

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
    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
    sessionStore = ((WebappPluginWorkspace) pluginWorkspace).getSessionStore();
    serviceAccount = loadServiceAccount(pluginWorkspace);
  }

  /**
   * Load the user credentials.
   * 
   * @param pluginWorkspace The plugin workspace.
   * 
   * @return The service account credentials.
   */
  private Optional<UserCredentials> loadServiceAccount(PluginWorkspace pluginWorkspace) {
    WSOptionsStorage optionsStorage = pluginWorkspace.getOptionsStorage();
    String cmisUser = Optional.ofNullable(System.getProperty(CMIS_SERVICE_USER_PROP))
        .orElse(optionsStorage.getOption(CMIS_SERVICE_USER_PROP, null));
    
    String cmisPassword = Optional.ofNullable(System.getProperty(CMIS_SERVICE_PASSWORD_PROP))
        .orElse(optionsStorage.getOption(CMIS_SERVICE_PASSWORD_PROP, null));
    
    if (cmisUser != null && cmisPassword != null) {
      return Optional.of(new UserCredentials(cmisUser, cmisPassword));
    } else {
      return Optional.empty();
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
  
  /**
   * @return true if a service account is configured.
   */
  public boolean hasServiceAccount() {
    return serviceAccount.isPresent();
  }
}
