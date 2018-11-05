package com.oxygenxml.cmis.web;

public interface TranslationTags {

	String SERVER_URL = "Server_URL";
	
	/**
	 * Warning for the 'Enforce server' setting. Used in WebDAV plugin
	 * configuration.
	 * 
	 * en: Note: Once a server is enforced, the user will only be able to browse
	 * this enforced server. However, it is possible for other plugins to add more
	 * enforced servers for the user to choose from.
	 */
	String CMIS_SERVER_NOTE = "Cmis_server_note";

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
	 * Server name - set from configuration page.
	 * 
	 * en: Server name
	 */
	String SERVER_NAME = "Server_name";

	/**
	 * Icon URL - set from configuration page.
	 * 
	 * en: Icon URL
	 */
	String SERVER_LOGO_URL = "Server_logo_URL";

	/**
	 * Message if opened document is an old version of actual document.
	 * 
	 * en: Old version of document
	 */
	String OLD_VER_WARNING = "Old_ver_warning";

	/**
	 * Message if document is checked out by another user.
	 * 
	 * en: Checked - out by
	 */
	String CHECKED_OUT_BY = "Checked_out_by";

	/**
	 * Message if check out is required.
	 * 
	 * en: Check - out required
	 */
	String CHECK_OUT_REQUIRED = "Check_out_required";

	/**
	 * Label for input. Used in WebDAV plugin configuration.
	 * 
	 * en: Autosave interval
	 */
	String AUTOSAVE_INTERVAL = "Autosave_interval";
	
	/**
	 * Complements the 'Autosave interval' input. Used in WebDAV plugin
	 * configuration.
	 * 
	 * en: seconds
	 */
	String SECONDS = "Seconds";
}
