package com.oxygenxml.cmis.ui;

import java.net.URL;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.storage.SessionStorage;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Utility for getting the user credentials and open the login dialog while
 * credentials are null
 * 
 * @author bluecc
 *
 */
public class AuthenticatorUtil {
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(AuthenticatorUtil.class);

  /**
   * While the credentials are null show the login dialog
   * 
   * 
   * @param serverURL
   * 
   * @exception UserCanceledException
   * 
   * @return UserCredentials
   */
  public static UserCredentials getUserCredentials(URL serverURL) throws UserCanceledException {
    UserCredentials uc = null;

    // Get the credentials using the serverURL
    uc = SessionStorage.getInstance().getUserCredentials(serverURL);

    if (logger.isDebugEnabled()) {
      logger.debug("user credentials " + uc);
    }

    logger.info("user credentials " + uc);

    // While no valid credentials the login dialog will appear
    while (uc == null) {

      // Initialize the Login Dialog
      LoginDialog loginDialog = new LoginDialog((JFrame) PluginWorkspaceProvider.getPluginWorkspace().getParentFrame());

      loginDialog.setVisible(true);

      // Check whether user pressed ok
      if (loginDialog.getResult() == LoginDialog.RESULT_OK) {

        // Get the user credentials
        uc = loginDialog.getUserCredentials();

        // Add the entered credentials to the session
        SessionStorage.getInstance().addUserCredentials(serverURL, uc);

      } else {
        throw new UserCanceledException();
      }

    }

    return uc;
  }
}
