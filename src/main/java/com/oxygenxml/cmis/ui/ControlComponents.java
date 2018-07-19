package com.oxygenxml.cmis.ui;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.*;

import com.oxygenxml.cmis.core.CMISAccess;

import java.util.*;

public class ControlComponents extends JPanel {

  private RepoComboBoxView repoComboBox;
  private ItemListView itemList;
  private BreadcrumbView breadcrumbList;
  private JPanel northPanel, southPanel, centerPanel;
  private ServerView serverPanel;
  private SearchView searchPanel;

  public ControlComponents(TabComponentsView tabs) {

    // Configure JPanel
    breadcrumbList = new BreadcrumbView(new ItemsPresenter() {

      @Override
      public void presentItems(URL connectionInfo, String repositoryID) {
        itemList.presentItems(connectionInfo, repositoryID);
      }

      @Override
      public void presentFolderItems(String folderID) {
        itemList.presentFolderItems(folderID);
      }
    });

    itemList = new ItemListView(tabs, breadcrumbList);
    searchPanel = new SearchView(itemList);
    repoComboBox = new RepoComboBoxView(itemList);
    serverPanel = new ServerView(repoComboBox);

    setMinimumSize(new Dimension(200, 200));
    setLayout(new GridLayout(2, 1));
    /*
     * Creation of the northPanel
     */
    northPanel = new JPanel();
    centerPanel = new JPanel();
    southPanel = new JPanel();

    // Set two 3 rows and 1 column
    northPanel.setLayout(new GridLayout(3, 1,0,0));
    northPanel.add(serverPanel);
    northPanel.add(repoComboBox);
    northPanel.add(searchPanel);

    // Set 2 row and 1 column
    southPanel.setLayout(new GridLayout(2, 1));
    southPanel.add(breadcrumbList);
    southPanel.add(itemList);


    // Add the northpanel to this frame
    add(northPanel, BorderLayout.NORTH);
    add(southPanel,BorderLayout.SOUTH);

  }

}
