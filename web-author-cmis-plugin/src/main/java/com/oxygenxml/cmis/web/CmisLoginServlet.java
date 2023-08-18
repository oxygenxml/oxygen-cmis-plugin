package com.oxygenxml.cmis.web;

import java.io.IOException;

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
			
			CredentialsManager.INSTANCE.setCredentials(userId, 
			    new UserCredentials(user, passwd, true));
		}
	}
}
