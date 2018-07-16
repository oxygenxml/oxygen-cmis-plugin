package com.oxygenxml.cmis.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SearchView extends JPanel {

  private ItemsPresenter itemsPresenter;

  public SearchView(ItemsPresenter itemsPresenter) {
    // TODO Auto-generated constructor stub

    this.itemsPresenter = itemsPresenter;
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Search JTextField constraints
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.9;
    c.gridwidth = 3;
    c.gridx = 0;
    c.gridy = 0;
    c.ipadx = 40;
    JTextField searchField = new JTextField("Search");
    add(searchField, c);

    // Search JButton constraints
    c.gridwidth = 0;
    c.gridx = 3;
    c.gridy = 0;
    c.weightx = 0.1;

    JButton searchButton = new JButton("Search");

    
    searchButton.addActionListener(new ActionListener() {
      /*
       * TODO: Implement the search using queries
       */
      @Override
      public void actionPerformed(ActionEvent e) {
        // itemsPresenter.presentItems(null,null);
      }
    });
    add(searchButton, c);
  }

}

