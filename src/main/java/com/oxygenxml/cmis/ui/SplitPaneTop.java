package com.oxygenxml.cmis.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.oxygenxml.cmis.core.CMISAccess;

import java.util.*;

public class SplitPaneTop {

	private JSplitPane splitPane;
	private ListRepoView listRepo;
	private ListItemView listItem; 


	public SplitPaneTop(TabComponentsView tabs) {
	   // Pane from the right with documents and folders
    listItem = new ListItemView(tabs);
    
	  // Pane from the left with repos
	  listRepo = new ListRepoView(listItem);

	
		// Create a split pane with the listRepoScrollPane and itemRepoScrollPane.
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listRepo, listItem);

		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);
		
		// Provide a preferred size for the split pane.
		splitPane.setPreferredSize(new Dimension(400, 400));

	}
	public RepositoriesPresenter getRepositoriesPresenter() {
		return listRepo;
	}
	public ItemsPresenter getItemsPresenter(){
	  return listItem;
	}

	public JSplitPane getSplitPane() {
		return splitPane;
	}

}
