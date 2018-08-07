package com.oxygenxml.cmis.search;

import java.util.ArrayList;

import org.apache.chemistry.opencmis.client.api.Folder;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

/**
 * @see com.oxygenxml.core.model.impl
 * @author bluecc
 *
 *         !!! DUNNO IF WORKS
 */
public class SearchDocument {

  private ArrayList<IResource> resultsQueries;
  private FolderImpl tempFolder;
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

    case "name":
      resultsQueries.addAll(searchCtrl.queringDoc(toSearch));
      break;

    default:
      break;
    }

  }

  public ArrayList<IResource> getResultsFolder() {
    return resultsQueries;
  }
}
