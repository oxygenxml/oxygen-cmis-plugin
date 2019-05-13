package com.oxygenxml.cmis.ui.actions;

import java.awt.event.ItemEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTextField;

import com.oxygenxml.cmis.search.SearchDocument;
import com.oxygenxml.cmis.storage.SearchScopeConstants;
import com.oxygenxml.cmis.storage.SessionStorage;
import com.oxygenxml.cmis.ui.ContentSearcher;

/**
 * Action that shows all the checked-out resources and uses a content searcher
 * and the textfield in order to get the correct input
 * 
 * @author bluecc
 * @see SearchDocument For the constants
 */
public class ShowCheckedoutResourcesMenuItem extends JCheckBoxMenuItem {
  private final transient ContentSearcher contentSearcher;
  private final JTextField searchText;
  public static final String PERSONAL_CHECKEDOUT_OPTION = "PERSONAL_CHECKEDOUT";

  public ShowCheckedoutResourcesMenuItem(ContentSearcher contentSearcher, JTextField searchText) {
    super("Show only personal checked-out documents");
    this.contentSearcher = contentSearcher;
    this.searchText = searchText;
    
    String option = SessionStorage.getInstance().getFilteringCriteria();
    if (SearchScopeConstants.SHOW_ONLY_PERSONAL_CHECKED_OUT.equals(option)) {
      setSelected(true);
    }
    
    addItemListener(this::perform);
  }

  public void perform(ItemEvent e) {
    String option = SessionStorage.getInstance().getFilteringCriteria();
    if (!SearchScopeConstants.SHOW_ONLY_PERSONAL_CHECKED_OUT.equals(option)) {
      // Another option was active.
      SessionStorage.getInstance().setFilteringCriteria(SearchScopeConstants.SHOW_ONLY_PERSONAL_CHECKED_OUT);
      contentSearcher.doSearch(searchText.getText().trim(), true);
    }
  
    // False option is for checking if you want and the folders
    contentSearcher.doSearch(searchText.getText().trim(), false);
  }

}
