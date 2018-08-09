package com.oxygenxml.web.cmis;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.WebappServletPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CmisLoginServlet extends WebappServletPluginExtension{

  @Override
  public String getPath() {
    // You can access this servlet extension at: OXYGEN_WEB_AUTHOR/plugins-dispatcher/servlet-path
    return "cmis-login";
  }

  @Override
  public void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
      throws ServletException, IOException { 
      String userId = httpRequest.getSession().getId();
      String action = httpRequest.getParameter("action");
      
      WebappPluginWorkspace workspace = (WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace();
      SessionStore sessionStore = workspace.getSessionStore();
      
      if ("logout".equals(action)) {
    	  sessionStore.remove(userId, "credentials");
      } else {
        String user = httpRequest.getParameter("user");
        String passwd = httpRequest.getParameter("passwd");
        
        sessionStore.put(userId, "credentials", new UserCredentials(user, passwd));
      }
  }
}
