package com.oxygenxml.cmis.search;

import java.util.ArrayList;
import java.util.List;

import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.storage.SearchScopeConstants;

/**
 * @see com.oxygenxml.core.model.impl
 * @author bluecc
 *
 * 
 */
public class SearchDocument {

  private final ArrayList<IResource> resultsQueries;

  /**
   * Search constructor for searching for different options (default null)
   * 
   * @param toSearch
   * @param searchCtrl
   * @param options
   */
  public SearchDocument(String toSearch, SearchController searchCtrl, String option) {
    this.resultsQueries = new ArrayList<>();
    switch (option) {
    case SearchScopeConstants.SHOW_ALL_OPTION:
      resultsQueries.addAll(searchCtrl.queryDoc(toSearch));
      break;
    case SearchScopeConstants.SHOW_ONLY_PERSONAL_CHECKED_OUT:
      resultsQueries.addAll(searchCtrl.queryPersonalCheckedout(toSearch));
      break;
    case SearchScopeConstants.SHOW_ONLY_FOREIGN_CHECKED_OUT:
      resultsQueries.addAll(searchCtrl.queryForeignCheckedoutDocs(toSearch));
      break;
    case "name":
      resultsQueries.addAll(searchCtrl.queryDocName(toSearch));
      break;

    default:
      break;
    }
  }

  /**
   * 
   * @return All the documents from the search.
   */
  public List<IResource> getResultsFolder() {
    return resultsQueries;
  }
}
