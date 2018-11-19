package com.oxygenxml.cmis.web;

public enum EditorOption {
	NON_VERSIONABLE("nonversionable"),
	
	IS_CHECKED_OUT("checkedout"),
	
	NO_SUPPORT("nosupportfor"),
	
	OLD_VERSION("oldversion"),
	
	TO_BLOCK("block");
	
	
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