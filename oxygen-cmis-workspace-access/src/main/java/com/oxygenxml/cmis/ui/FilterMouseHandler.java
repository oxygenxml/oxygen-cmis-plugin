package com.oxygenxml.cmis.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.oxygenxml.cmis.ui.actions.ShowAllResourcesMenuItem;
import com.oxygenxml.cmis.ui.actions.ShowCheckedoutResourcesMenuItem;
import com.oxygenxml.cmis.ui.actions.ShowForeignCheckoutResourcesMenuItem;

public class FilterMouseHandler extends MouseAdapter {
  private final ContentSearcher contentSearcher;
  private final JTextField searchText;

  FilterMouseHandler(ContentSearcher contentSearcher, JTextField searchText) {
    this.contentSearcher = contentSearcher;
    this.searchText = searchText;
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    // If right click was pressed
    if (event.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(event) && event.getComponent().isEnabled()) {

      JPopupMenu menu = new JPopupMenu();
      menu.add(new ShowAllResourcesMenuItem(contentSearcher,searchText));
      menu.add(new ShowCheckedoutResourcesMenuItem(contentSearcher,searchText));
      menu.add(new ShowForeignCheckoutResourcesMenuItem(contentSearcher,searchText));
      
      menu.show(event.getComponent(), event.getX(), event.getY());
    }
  }

}
