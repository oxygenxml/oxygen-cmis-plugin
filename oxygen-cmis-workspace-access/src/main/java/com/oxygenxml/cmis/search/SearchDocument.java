package com.oxygenxml.cmis.search;

import java.util.ArrayList;

import org.apache.chemistry.opencmis.client.api.Folder;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

/**
 * @see com.oxygenxml.core.model.impl
 * @author bluecc
 *
 * 
 */
public class SearchDocument {

  private final ArrayList<IResource> resultsQueries;
  private final FolderImpl tempFolder;
  private Folder tempRoot;

  /**
   * 
   * @param toSearch
   * @param searchCtrl
   * @param options
   */
  public SearchDocument(String toSearch, SearchController searchCtrl, String option) {



    this.resultsQueries = new ArrayList<>();
    this.tempFolder = new FolderImpl(CMISAccess.getInstance().createResourceController().getRootFolder());

    switch (option) {

    case "null":
//      for (String key : searchKeys) {
//        System.out.println("The key= "+key);
        resultsQueries.addAll(searchCtrl.queryDoc(toSearch));
//      }
      break;

    case "name":
      resultsQueries.addAll(searchCtrl.queryDocName(toSearch));
      break;

    default:
      break;
    }

  }

  public ArrayList<IResource> getResultsFolder() {
    return resultsQueries;
  }
}
