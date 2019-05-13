package com.oxygenxml.cmis.storage;

/**
 * Possible filtering criteria. 
 */
public interface SearchConstants {
  /**
   * Show all documents that match the search criteria.
   */
  public static final String SHOW_ALL_OPTION = "all";
  /**
   * Show only the documents that match the search criteria and are checked out by me.
   */
  public static final String SHOW_ONLY_PERSONAL_CHECKED_OUT = "personal";
  /**
   * Show only the documents that match the search criteria and are checked out by someone else.
   */
  public static final String SHOW_ONLY_FOREIGN_CHECKED_OUT = "foreign";
  

}
