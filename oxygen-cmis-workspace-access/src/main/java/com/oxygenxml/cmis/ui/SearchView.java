package com.oxygenxml.cmis.ui;

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
public class SearchView extends JPanel {

  private String option = null;

  public SearchView(ItemsPresenter itemsPresenter) {

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Search JTextField constraints
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridwidth = 3;
    c.gridx = 0;
    c.gridy = 0;
    c.ipadx = 40;
    c.insets = new Insets(1, 5, 1, 5);
    JTextField searchField = new JTextField("Search");
    add(searchField, c);

    // Search JButton constraints
    c.gridwidth = 0;
    c.gridx = 3;
    c.gridy = 0;
    c.weightx = 0.0;

    JButton searchButton = new JButton("Search");

    searchButton.addActionListener(new ActionListener() {

      /*
       * TODO: Implement the search using queries
       */

      @Override
      public void actionPerformed(ActionEvent e) {

        // Set the default option
        option = "name";


        final String searchText = searchField.getText().trim();
        itemsPresenter.presentFolderItems(new IFolder() {
          List<IResource> queryResults = null;
          @Override
          public Iterator<IResource> iterator() {
            if (queryResults == null) {
              queryResults = searchItems(searchText);
            }
            return queryResults.iterator();
          }

          @Override
          public String getId() {
            return "#search.results";
          }

          @Override
          public String getDisplayName() {
            return "SearchResults";
          }

          @Override
          public String getFolderPath() {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public String getCreatedBy() {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public boolean isCheckedOut() {
            // TODO Auto-generated method stub
            return false;
          }

          @Override
          public void refresh() {
            queryResults = null;
          }
        });
      }

      private List<IResource> searchItems(String searchText) {
        List<IResource> queryResults = new ArrayList<>();
        SearchController searchCtrl = new SearchController(CMISAccess.getInstance().createResourceController());

        // The results from searching the documents
        ArrayList<IResource> documentsResults = new SearchDocument(searchText, searchCtrl, option)
            .getResultsFolder();

        // The results from searching the folders
        ArrayList<IResource> foldersResults = new SearchFolder(searchText, searchCtrl, option)
            .getResultsFolder();

        queryResults.addAll(documentsResults);
        queryResults.addAll(foldersResults);
        
        return queryResults;
      }
    });
    add(searchButton, c);
  }

}
