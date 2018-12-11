package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.search.SearchDocument;
import com.oxygenxml.cmis.ui.ContentSearcher;

/**
 * Action that shows all the resources and uses a content searcher and the
 * textfield in order to get the correct input.
 * 
 * @author bluecc
 *@see SearchDocument For the constants
 */
public class ShowAllResourcesAction extends AbstractAction {
  public static final String ALL_OPTION = "null";
  
  private final transient ContentSearcher contentSearcher;
  private final JTextField searchText;
  /**
   * Logger
   */
  private static final Logger logger = Logger.getLogger(ShowAllResourcesAction.class);

  public ShowAllResourcesAction(ContentSearcher contentSearcher, JTextField searchText) {
    super("Show all resources");
    this.contentSearcher = contentSearcher;
    this.searchText = searchText;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    logger.debug("Search key=" + searchText.getText().trim());
    contentSearcher.doSearch(searchText.getText().trim(), ALL_OPTION, true);
  }

}
