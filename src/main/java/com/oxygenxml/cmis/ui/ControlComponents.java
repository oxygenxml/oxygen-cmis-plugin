package com.oxygenxml.cmis.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.oxygenxml.cmis.core.CMISAccess;

import java.util.*;

public class ControlComponents extends JPanel {

  private RepoComboBoxView repoComboBox;
  private ItemListView itemList;
  private JPanel northPanel, southPanel, serverPanel, searchPanel;

  public ControlComponents(TabComponentsView tabs) {

    // Configure JPanel
    itemList = new ItemListView(tabs);
    searchPanel = new SearchView(itemList);
    repoComboBox = new RepoComboBoxView(itemList);
    serverPanel = new ServerView(repoComboBox);

    setMinimumSize(new Dimension(200, 200));
    setLayout(new GridLayout(2, 1));
    /*
     * Creation of the northPanel
     */
    northPanel = new JPanel();
    southPanel = new JPanel();

    // Set two 3 rows and 1 column
    northPanel.setLayout(new GridLayout(3, 1));
    northPanel.add(serverPanel);
    northPanel.add(repoComboBox);
    northPanel.add(searchPanel);

    // Set 1 row and 1 column
    southPanel.setLayout(new GridLayout(1, 1));
    southPanel.add(itemList);
    //
    // // Add the northpanel to this frame
     add(northPanel, BorderLayout.NORTH);
     add(southPanel,BorderLayout.CENTER);

  }

  public RepositoriesPresenter getRepositoriesPresenter() {
    return repoComboBox;
  }

  public ItemsPresenter getItemsPresenter() {
    return itemList;
  }

  // public JSplitPane getSplitPane() {
  // return splitPane;
  // }

}
