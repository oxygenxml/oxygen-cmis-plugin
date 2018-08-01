package com.oxygenxml.cmis.ui;

import java.net.URL;

import javax.swing.JFrame;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.storage.SessionStorage;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class AuthenticatorUtil {

  /**
   *  While the credentials are null show the login dialog
   * 
   * 
   * @param serverURL
   * 
   */
  public static UserCredentials getUserCredentials(URL serverURL) throws UserCanceledException {
    UserCredentials uc = null;

    uc = SessionStorage.getInstance().getUserCredentials(serverURL);
    
    System.out.println("user credentials " + uc);

    while (uc == null) {
      
      //Initialize the Login Dialog
      LoginDialog loginDialog = new LoginDialog((JFrame) PluginWorkspaceProvider.getPluginWorkspace().getParentFrame());

      loginDialog.setVisible(true);

      //Check whether user pressed ok
      if (loginDialog.getResult() == LoginDialog.RESULT_OK) {
        
        uc = loginDialog.getUserCredentials();
        
        SessionStorage.getInstance().addUserCredentials(serverURL, uc);
        
      } else {
        throw new UserCanceledException();
      }

    }

    return uc;
  }
}
