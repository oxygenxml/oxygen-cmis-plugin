package com.oxygenxml.cmis.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IResource;
public class SearchView extends JPanel {

  private ItemsPresenter itemsPresenter;
  private List<IResource> queryResults  = new ArrayList<IResource>();
  public SearchView(ItemsPresenter itemsPresenter) {

    this.itemsPresenter = itemsPresenter;
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Search JTextField constraints
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridwidth = 3;
    c.gridx = 0;
    c.gridy = 0;
    c.ipadx = 40;
    c.insets = new Insets(1,5,1,5);
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
      /*
       * TODO: Get the item by name 
       */
      @Override
      public void actionPerformed(ActionEvent e) {
        SearchController searchCtrl = new SearchController(CMISAccess.getInstance().createResourceController());
        //searchCtrl.
        // itemsPresenter.presentItems(null,null);
      }
    });
    add(searchButton, c);
  }

  
}

