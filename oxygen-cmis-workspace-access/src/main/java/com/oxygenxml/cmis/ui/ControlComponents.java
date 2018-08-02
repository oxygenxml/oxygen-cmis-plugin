package com.oxygenxml.cmis.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;

import javax.swing.JPanel;

public class ControlComponents extends JPanel {

  private RepoComboBoxView repoComboBox;
  private ItemListView itemsPanel;
  private BreadcrumbView breadcrumbList;
  private ServerView serverPanel;
  private SearchView searchPanel;

  public ControlComponents(TabsPresenter tabs) {

    // Configure the breadcrumb for initialization
    breadcrumbList = new BreadcrumbView(new ItemsPresenter() {

      @Override
      public void presentItems(URL connectionInfo, String repositoryID) {
        itemsPanel.presentItems(connectionInfo, repositoryID);
      }

      @Override
      public void presentFolderItems(String folderID) {
        itemsPanel.presentFolderItems(folderID);
      }
    });

    itemsPanel = new ItemListView(tabs, breadcrumbList);
    searchPanel = new SearchView(itemsPanel);
    repoComboBox = new RepoComboBoxView(itemsPanel, breadcrumbList);
    serverPanel = new ServerView(repoComboBox);

    setMinimumSize(new Dimension(200, 250));
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
    c.insets = new Insets(10, 0, 5, 0);
    // c.weighty = 0.5;
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
