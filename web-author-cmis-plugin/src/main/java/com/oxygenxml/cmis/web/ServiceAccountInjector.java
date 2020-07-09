package com.oxygenxml.cmis.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.exml.plugin.PluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Injects service account credentials on the current session.
 * 
 * @author cristi_talau
 */
public class ServiceAccountInjector implements Filter, PluginExtension {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Nothing to do here.
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String cmisUser = "test"; //System.getProperty("cmis.service.user");
    String cmisPassword = "gh678j"; //  System.getProperty("cmis.service.password");

    if (cmisUser != null && cmisPassword != null) {
      WebappPluginWorkspace webappPluginWorkspace = (WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace();
      SessionStore sessionStore = webappPluginWorkspace.getSessionStore();
      UserCredentials creds = new UserCredentials(cmisUser, cmisPassword);
      String sessionId = ((HttpServletRequest)request).getSession().getId();
      sessionStore.put(sessionId, "wa-cmis-plugin-credentials", creds);
    }
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // Nothing to do here
  }
}
