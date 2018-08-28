package com.oxygenxml.cmis.search;

import java.util.ArrayList;

import org.apache.chemistry.opencmis.client.api.Folder;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

/**
 * @see com.oxygenxml.core.model.impl
 * @author bluecc
 *
 */
public class SearchFolder {

  private ArrayList<IResource> resultsQueries;

  /**
   * 
   * @param toSearch
   * @param searchCtrl
   * @param options
   */
  public SearchFolder(String toSearch, SearchController searchCtrl, String option) {
    String [] searchKeys= toSearch.split("\\s+");
    this.resultsQueries = new ArrayList<IResource>();

    switch (option) {

    case "null":
      for (String key : searchKeys) {
        System.out.println("The key= "+key);
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

  public ArrayList<IResource> getResultsFolder() {
    return resultsQueries;
  }
}
