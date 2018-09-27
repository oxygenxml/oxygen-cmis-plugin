package com.oxygenxml.cmis.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.oxygenxml.cmis.actions.ShowAllResourcesAction;
import com.oxygenxml.cmis.actions.ShowCheckedoutResourcesAction;
import com.oxygenxml.cmis.actions.ShowForeignCheckoutResourcesAction;

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
      menu.add(new ShowAllResourcesAction(contentSearcher,searchText));
      menu.add(new ShowCheckedoutResourcesAction(contentSearcher,searchText));
      menu.add(new ShowForeignCheckoutResourcesAction(contentSearcher,searchText));
      menu.show(event.getComponent(), event.getX(), event.getY());
    }
  }

}
