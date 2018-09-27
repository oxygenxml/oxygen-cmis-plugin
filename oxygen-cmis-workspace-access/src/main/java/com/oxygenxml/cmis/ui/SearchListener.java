package com.oxygenxml.cmis.ui;

import java.util.List;

import com.oxygenxml.cmis.core.model.IResource;

/**
 * Search interface for search resources that handles the shadow IFolder
 * 
 * @see com.oxygenxml.cmis.core.model
 * @author bluecc
 *
 */
public interface SearchListener {
  /**
   * Installs the search resources for the: custom shadow folder of the search
   * results, cached data and installs the custom renderer.
   * 
   * @param filter
   * @param resources
   * @param option
   * @param searchFolders
   *          Default search will be only for documents.
   */
  void searchFinished(String filter, List<IResource> resources, String option, boolean searchFolders);

}
