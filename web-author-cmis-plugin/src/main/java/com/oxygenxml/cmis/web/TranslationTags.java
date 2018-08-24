package com.oxygenxml.cmis.web;

public interface TranslationTags {
	
	String SERVER_URL = "Server_URL";

	String CHECKOUT_REQUIRED_RESOURCE = "Checkout_required";
	/**
	 * Label for input. Used in WebDAV plugin configuration.
	 * 
	 * en: Enforced server
	 */
	String ENFORCED_SERVER = "Enforced_server";
	/**
	 * Warning for the 'Enforce server' setting. Used in WebDAV plugin
	 * configuration.
	 * 
	 * en: Note: Once a server is enforced, the user will only be able to browse
	 * this enforced server. However, it is possible for other plugins to add more
	 * enforced servers for the user to choose from.
	 */
	String ENFORCED_SERVER_NOTE = "Enforced_server_note";

	/**
	 * Title of login dialog.
	 * 
	 * en: Authentication required
	 */
	String AUTHENTICATION_REQUIRED = "Authentication_required";

	/**
	 * Default author name for comments.
	 * 
	 * en: Anonymous
	 */
	String ANONYMOUS = "Anonymous";
	
	/**
	 * 
	 */
	String SERVER_NAME = "Server_name";
	
	/**
	 * 
	 */
	String ICON_URL = "Icon_URL";
}
