package com.oxygenxml.cmis.ui.actions;

import java.awt.event.ItemEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTextField;

import com.oxygenxml.cmis.storage.SearchConstants;
import com.oxygenxml.cmis.storage.SessionStorage;
import com.oxygenxml.cmis.ui.ContentSearcher;

/**
 * Action that shows all the resources and uses a content searcher and the
 * textfield in order to get the correct input.
 * 
 * @author bluecc
 */
public class ShowAllResourcesMenuItem extends JCheckBoxMenuItem {
  private final transient ContentSearcher contentSearcher;
  private final JTextField searchText;
  
  /**
   * Constructor.
   *  
   * @param contentSearcher
   * @param searchText
   */
  public ShowAllResourcesMenuItem(ContentSearcher contentSearcher, JTextField searchText) {
    super("Show all resources");
    this.contentSearcher = contentSearcher;
    this.searchText = searchText;
    
    String option = SessionStorage.getInstance().getFilteringCriteria();
    if (option == null || SearchConstants.SHOW_ALL_OPTION.equals(option)) {
      setState(Boolean.TRUE);
    }
    
    addItemListener(e -> {
      itemSelected(e);
    });
  }

  public void itemSelected(ItemEvent e) {
    String option = SessionStorage.getInstance().getFilteringCriteria();
    if (option != null && !SearchConstants.SHOW_ALL_OPTION.equals(option)) {
      // Another option was active.
      SessionStorage.getInstance().setFilteringCriteria(SearchConstants.SHOW_ALL_OPTION);
      contentSearcher.doSearch(searchText.getText().trim(), true);
    }
  }
}
