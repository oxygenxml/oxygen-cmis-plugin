package com.oxygenxml.cmis.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.chemistry.opencmis.client.api.Folder;
//import org.apache.chemistry.opencmis.client.api.ObjectId;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.search.SearchDocument;
import com.oxygenxml.cmis.search.SearchFolder;
import com.oxygenxml.cmis.storage.SessionStorage;
import com.oxygenxml.cmis.core.CMISAccess;

/**
 * Search componenet that takes care of the searching for resources
 * 
 * @author bluecc
 *
 */
public class SearchView extends JPanel implements ContentSearchProvider, SearchPresenter {
  /**
   * Objects interested in search events.
   */

  // Listeners of the search behavior
  private List<SearchListener> listeners = new ArrayList<>();

  private JTextField searchField = null;
  private JButton searchButton = null;

  // Option of the search (name, title)
  private String option = null;

  public SearchView(ItemsPresenter itemsPresenter) {
    setOpaque(true);
    setBackground(Color.CYAN);

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Search JTextField constraints
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridwidth = 3;
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(1, 10, 1, 10);
    searchField = new JTextField("Search");
    searchField.setOpaque(true);
    searchField.setBackground(Color.red);
    searchField.setEnabled(false);
    add(searchField, c);

    // Search JButton constraints
    c.gridwidth = 0;
    c.gridx = 3;
    c.gridy = 0;
    c.weightx = 0.0;

    searchButton = new JButton("Search");
    searchButton.setEnabled(false);

    /**
     * This is where the fireSearch will occur when the button is pressed
     */
    searchButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        // The option will be orderer
        option = "null";
        // Get the entered text and trim of white space from both sides
        final String searchText = searchField.getText().trim();

        doSearch(searchText);
      }

    });
    searchButton.setOpaque(true);
    searchButton.setBackground(Color.blue);
    add(searchButton, c);
  }

  public void doSearch(final String searchText) {
    // Get the search results of the query
    List<IResource> queryResults = searchItems(searchText);

    // Fire the search for each listener
    fireSearchFinished(searchText, queryResults);
  }

  /**
   * Searches async for each listener
   * 
   * @param searchText
   * @param queryResults
   */
  protected void fireSearchFinished(String searchText, List<IResource> queryResults) {
    for (SearchListener l : listeners) {
      l.searchFinished(searchText, queryResults);
    }
  }

  /**
   * Add from outside those listeners here to be used for search
   * 
   * @param searchListener
   */
  public void addSearchListener(SearchListener searchListener) {
    listeners.add(searchListener);
  }

  /**
   * Add all the data searched in a list
   * 
   * @param searchText
   * @return
   */
  private List<IResource> searchItems(String searchText) {
    List<IResource> queryResults = new ArrayList<>();
    SearchController searchCtrl = new SearchController(CMISAccess.getInstance().createResourceController());

    // The results from searching the documents
    ArrayList<IResource> documentsResults = new SearchDocument(searchText, searchCtrl, option).getResultsFolder();

    for (IResource iResource : documentsResults) {
      System.out.println(" Doc id = " + iResource.getId());
      System.out.println(" Doc name = " + iResource.getDisplayName());
      System.out.println();
    }
    
    System.out.println("Documents=" + documentsResults.size());

    // The results from searching the folders
    ArrayList<IResource> foldersResults = new SearchFolder(searchText, searchCtrl, option).getResultsFolder();

    queryResults.addAll(documentsResults);
    queryResults.addAll(foldersResults);
    System.out.println("Results from server=" + queryResults.size());

    return queryResults;
  }

  /**
   * Find the line where the text was found
   */
  @Override
  public String getLineDoc(IResource doc, String matchPattern) {
    SearchController searchCtrl = new SearchController(CMISAccess.getInstance().clone().createResourceController());

    return searchCtrl.queryFindLine(doc, matchPattern);
  }

  @Override
  public String getPath(IResource doc, ResourceController ctrl) {

    return ((DocumentImpl) doc).getDocumentPath(ctrl);
  }

  @Override
  public void activateSearch() {
    searchField.setEnabled(true);
    searchButton.setEnabled(true);
  }

  @Override
  public String getProperties(IResource doc) {

    return ((DocumentImpl) doc).getDescription();
  }

  @Override
  public String getName(IResource resource) {
    return resource.getDisplayName();
  }

  // @Override
  // public List<String> getLinesDocuments() {
  //
  // ResourceController ctrl =
  // CMISAccess.getInstance().createResourceController();
  // SearchController searchCtrl = new SearchController(ctrl);
  //
  // final String searchText = searchField.getText().trim();
  //
  // List<IResource> docList = searchItems(searchText);
  //
  // return searchCtrl.queryFindLineContent(docList, searchText);
  //
  // }

}
