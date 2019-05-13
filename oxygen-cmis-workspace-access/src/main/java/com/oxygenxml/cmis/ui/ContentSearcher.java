package com.oxygenxml.cmis.ui;

import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;

/**
 * Search support.
 */
public interface ContentSearcher {
  /**
   * Gets the string formatted in limit of 45 in the first line where the
   * pattern were matched
   * 
   * @param doc
   * @param matchPattern
   * @return String formatted in the limit of 45 characters
   */
  String getLineDoc(IResource doc, String matchPattern);

  /**
   * Gets the path of the document
   * 
   * @param doc
   * @param ctrl
   * @return String path of the document
   */
  String getPath(IResource doc, ResourceController ctrl);

  /**
   * Gets property of the resource (document or folder)
   * 
   * @param resource
   * @return Custom string property
   */
  String getProperties(IResource resource);

  /**
   * Gets the name of the resource
   * 
   * @param resource
   * @return String name of the resource
   */
  String getName(IResource resource);

  /**
   * Add from outside those listeners here to be used for search
   * 
   * @param searchListener
   */
  void addSearchListener(SearchListener searchListener);

  /**
   * Activates the search of the searchText
   * 
   * @param searchText
   * @param searchFolders
   *          <code>False</code> Will search only for documents,
   *          <code>True</code> Will search for documents and folders.
   */
  public void doSearch(String searchText, boolean searchFolders);
}
