package com.oxygenxml.cmis.search;

import java.util.ArrayList;
import java.util.List;

import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IResource;

/**
 * @see com.oxygenxml.core.model.impl
 * @author bluecc
 *
 */
public class SearchFolder {

  private final ArrayList<IResource> resultsQueries;

  /**
   * Search constructor for searching for different options (default null).
   * 
   * @param toSearch
   * @param searchCtrl
   * @param options
   */
  public SearchFolder(String toSearch, SearchController searchCtrl, String option) {

    this.resultsQueries = new ArrayList<>();

    switch (option) {

    case "null":

      resultsQueries.addAll(searchCtrl.queryFolder(toSearch));

      break;

    case "name":
      resultsQueries.addAll(searchCtrl.queryFolderName(toSearch));
      break;

    default:
      break;
    }

  }

  /**
   * 
   * @return Returns all the folders from the search.
   */
  public List<IResource> getResultsFolder() {
    return resultsQueries;
  }
}
