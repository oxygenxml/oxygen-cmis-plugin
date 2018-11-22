package com.oxygenxml.cmis.web;

public enum EditorOption {
	NON_VERSIONABLE("nonversionable"),
	
	IS_CHECKED_OUT("checkedout"),
	
	SUPPORTS_COMMIT_MESSAGE("supports-commit-message"),
	
	OLD_VERSION("oldversion"),
	
	LOCKED("locked");
	
	
	private final String value;
	
	/**
	 * Not meant to be instantiated.
	 */
	private EditorOption(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
}