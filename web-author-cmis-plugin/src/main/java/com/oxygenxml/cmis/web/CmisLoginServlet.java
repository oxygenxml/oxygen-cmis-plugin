package com.oxygenxml.cmis.web;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.SessionParameter;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.CmisCredentials;
import com.oxygenxml.cmis.core.TokenCredentials;
import com.oxygenxml.cmis.core.UserCredentials;

import lombok.extern.slf4j.Slf4j;
import ro.sync.ecss.extensions.api.webapp.plugin.ServletPluginExtension;
import ro.sync.ecss.extensions.api.webapp.plugin.servlet.ServletException;
import ro.sync.ecss.extensions.api.webapp.plugin.servlet.http.HttpServletRequest;
import ro.sync.ecss.extensions.api.webapp.plugin.servlet.http.HttpServletResponse;

@Slf4j
public class CmisLoginServlet extends ServletPluginExtension {

  @Override
  public String getPath() {
    // You can access this servlet extension at:
    // OXYGEN_WEB_AUTHOR/plugins-dispatcher/servlet-path
    return "cmis-login";
  }

  /**
   * Get UserCredentials and put it on sessionStore.
   * 
   */
  @Override
  public void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
      throws ServletException, IOException {
    
    String userId = httpRequest.getSession().getId();
    String action = httpRequest.getParameter("action");

    log.info("CmisLoginServlet.doPost() userId --->" + userId + " action ---> " + action);

    if ("logout".equals(action)) {
      CredentialsManager.INSTANCE.forgetUserCredentials(userId);
    } else {
      String user = httpRequest.getParameter("user");
      String passwd = httpRequest.getParameter("passwd");
      
      CmisCredentials credentials;
      if (user == null || user.isEmpty() || user.toUpperCase().equals("ROLE_TICKET")) {
        String alfrescoTicket = passwd;
        String serverUrl = httpRequest.getParameter("serverUrl");
        if (serverUrl.endsWith("/")) {
          serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }
        AlfrescoApi alfrescoApi = new AlfrescoApi(new URL(serverUrl));
        AlfrescoPeople me = alfrescoApi.getMe(alfrescoTicket);
        
        if (me.getId() != null) {
          credentials = new TokenCredentials(passwd, me.getId());
        } else {
          credentials = new TokenCredentials(passwd, "ROLE_TICKET");
        }
      } else {
        credentials = new UserCredentials(user, passwd, true);
      }
      CredentialsManager.INSTANCE.setCredentials(userId, credentials);
      httpResponse.getWriter().write("{\"userName\": \"" + credentials.getUsername() + "\"}");
    }
  }
}
