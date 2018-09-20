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
   * 
   * @param toSearch
   * @param searchCtrl
   * @param options
   */
  public SearchFolder(String toSearch, SearchController searchCtrl, String option) {
    String [] searchKeys= toSearch.split("\\s+");
    this.resultsQueries = new ArrayList<>();

    switch (option) {

    case "null":
      for (String key : searchKeys) {
        resultsQueries.addAll(searchCtrl.queryFolder(key));
      }
      break;

    case "name":
      resultsQueries.addAll(searchCtrl.queryFolderName(toSearch));
      break;

    default:
      break;
    }

  }

  public List<IResource> getResultsFolder() {
    return resultsQueries;
  }
}
