package com.oxygenxml.cmis.ui.actions;

import java.awt.event.ItemEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTextField;

import com.oxygenxml.cmis.search.SearchDocument;
import com.oxygenxml.cmis.storage.SearchConstants;
import com.oxygenxml.cmis.storage.SessionStorage;
import com.oxygenxml.cmis.ui.ContentSearcher;

/**
 * Action that shows all the checked-out foreign resources and uses a content
 * searcher and the textfield in order to get the correct input
 * 
 * @author bluecc
 * @see SearchDocument For the constants
 */
public class ShowForeignCheckoutResourcesMenuItem extends JCheckBoxMenuItem {
  private final transient ContentSearcher contentSearcher;
  private final JTextField searchText;
  public static final String FOREIGN_OPTION = "FOREIGN";

  public ShowForeignCheckoutResourcesMenuItem(ContentSearcher contentSearcher, JTextField searchText) {
    super("Show only foreign checked-out documents");
    this.contentSearcher = contentSearcher;
    this.searchText = searchText;
    
    String option = SessionStorage.getInstance().getFilteringCriteria();
    if (SearchConstants.SHOW_ONLY_FOREIGN_CHECKED_OUT.equals(option)) {
      setSelected(true);
    }
    
    addItemListener(this::perform);
  }

  public void perform(ItemEvent e) {
    String option = SessionStorage.getInstance().getFilteringCriteria();
    if (!SearchConstants.SHOW_ONLY_FOREIGN_CHECKED_OUT.equals(option)) {
      // Another option was active.
      SessionStorage.getInstance().setFilteringCriteria(SearchConstants.SHOW_ONLY_FOREIGN_CHECKED_OUT);
      // True option is for checking if you want and the folders
      contentSearcher.doSearch(searchText.getText().trim(), true);
    }
  
  }

}
