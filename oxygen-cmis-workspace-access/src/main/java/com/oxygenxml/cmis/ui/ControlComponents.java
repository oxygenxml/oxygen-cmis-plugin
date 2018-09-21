package com.oxygenxml.cmis.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;

import javax.swing.JPanel;

import com.oxygenxml.cmis.core.model.IResource;

/**
 * Class that controls every component from the app Holds the main layout of the
 * view.
 * 
 * @author bluecc
 *
 */
public class ControlComponents extends JPanel {
  // Repository component
  private final RepoComboBoxView repoComboBox;
  // Documents and Folders componenet
  private final ItemListView itemsPanel;
  // Breadcrumb component
  private final BreadcrumbView breadcrumbList;
  // Server component
  private final ServerView serverPanel;
  // Search panel component
  private final SearchView searchPanel;

  /**
   * Receives the tab presenter to know where to present
   * 
   * @param tabs
   */
  public ControlComponents(TabsPresenter tabs) {

    // Configure the breadcrumb for initialization
    breadcrumbList = new BreadcrumbView(new ResourcesBrowser() {

      @Override
      public void presentResources(URL connectionInfo, String repositoryID) {
        itemsPanel.presentResources(connectionInfo, repositoryID);
      }

      @Override
      public void presentResources(String folderID) {
        itemsPanel.presentResources(folderID);
      }

      @Override
      public void presentResources(IResource parentResource) {
        itemsPanel.presentResources(parentResource);

      }
    });

    // Initialization of the items
    itemsPanel = new ItemListView(tabs, breadcrumbList);

    // Initialization of the search
    searchPanel = new SearchView();
  
    itemsPanel.setContentProvider(searchPanel);

    // Initialization of the repositories
    repoComboBox = new RepoComboBoxView();
    
    repoComboBox.addRepositoryListener(itemsPanel);
    repoComboBox.addRepositoryListener(breadcrumbList);

    // initialization of the server
    serverPanel = new ServerView(repoComboBox, searchPanel);

    // Visual configuration
    setMinimumSize(new Dimension(400, 250));
    setLayout(new GridBagLayout());

    /*
     * Creation of the northPanel
     */

    GridBagConstraints c = new GridBagConstraints();

    // serverPanel
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    add(serverPanel, c);

    // repoComboBox
    c.gridy++;
    add(repoComboBox, c);

    // searchPanel
    c.gridy++;
    add(searchPanel, c);

    /*
     * Creation of the southPanel
     */

    // breadcrumbList
    c.gridy++;
    c.insets = new Insets(5, 0, 5, 0);
    add(breadcrumbList, c);

    // itemList
    c.gridy++;
    c.weighty = 1.0;
    c.insets = new Insets(0, 0, 0, 0);

    // c.weighty and weightx depends on c.fill V or H
    c.fill = GridBagConstraints.BOTH;
    add(itemsPanel, c);

  }

}
