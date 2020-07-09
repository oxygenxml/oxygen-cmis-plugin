package com.oxygenxml.cmis.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.ecss.extensions.api.webapp.plugin.WebappServletPluginExtension;

public class CmisLoginServlet extends WebappServletPluginExtension {

	private static final Logger logger = Logger.getLogger(CmisLoginServlet.class.getName());

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

		logger.info("CmisLoginServlet.doPost() userId --->" + userId + " action ---> " + action);

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
