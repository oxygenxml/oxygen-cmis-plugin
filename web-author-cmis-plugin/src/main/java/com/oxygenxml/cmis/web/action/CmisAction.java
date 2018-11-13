package com.oxygenxml.cmis.web.action;

public enum CmisAction {
	
	ACTION("action"),

	CHECK_OUT("cmisCheckout"),
	
	CHECK_IN("cmisCheckin"),
	
	CANCEL_CHECK_OUT("cancelCmisCheckout"),
	
	LIST_VERSIONS("listOldVersions"),
	
	COMMIT_MESSAGE("commit"),
	
	STATE("state"),
	
	MAJOR_STATE("major"),
	
	OLD_VERSION("oldversion");
	
	
	/**
	 * Value of the action as String.
	 */
	private final String value;
	
	/**
	 * Not meant to be instantiated.
	 */
	private CmisAction(String value) {
		this.value = value;
	}
	
	/**
	 * 
	 * @return value of Action as String.
	 */
	public String getValue() {
		return this.value;
	}
}
