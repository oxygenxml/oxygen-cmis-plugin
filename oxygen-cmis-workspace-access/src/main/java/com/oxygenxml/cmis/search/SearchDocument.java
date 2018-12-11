package com.oxygenxml.cmis.search;

import java.util.ArrayList;
import java.util.List;

import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.ui.actions.ShowAllResourcesAction;
import com.oxygenxml.cmis.ui.actions.ShowCheckedoutResourcesAction;
import com.oxygenxml.cmis.ui.actions.ShowForeignCheckoutResourcesAction;

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

    case ShowAllResourcesAction.ALL_OPTION:

      resultsQueries.addAll(searchCtrl.queryDoc(toSearch));
      break;

    case ShowCheckedoutResourcesAction.PERSONAL_CHECKEDOUT_OPTION:

      resultsQueries.addAll(searchCtrl.queryPersonalCheckedout(toSearch));
      break;
    case ShowForeignCheckoutResourcesAction.FOREIGN_OPTION:

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
