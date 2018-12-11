package com.oxygenxml.cmis.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextField;

import com.oxygenxml.cmis.search.SearchDocument;
import com.oxygenxml.cmis.ui.ContentSearcher;

/**
 * Action that shows all the checked-out foreign resources and uses a content
 * searcher and the textfield in order to get the correct input
 * 
 * @author bluecc
 * @see SearchDocument For the constants
 */
public class ShowForeignCheckoutResourcesAction extends AbstractAction {
  private final transient ContentSearcher contentSearcher;
  private final JTextField searchText;
  public static final String FOREIGN_OPTION = "FOREIGN";

  public ShowForeignCheckoutResourcesAction(ContentSearcher contentSearcher, JTextField searchText) {
    super("Show foreign checked-out documents");
    this.contentSearcher = contentSearcher;
    this.searchText = searchText;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // True option is for checking if you want and the folders
    contentSearcher.doSearch(searchText.getText().trim(), FOREIGN_OPTION, true);
  }

}
