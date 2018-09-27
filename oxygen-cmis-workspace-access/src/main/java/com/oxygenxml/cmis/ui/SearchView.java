package com.oxygenxml.cmis.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.plugin.TranslationResourceController;
import com.oxygenxml.cmis.search.SearchDocument;
import com.oxygenxml.cmis.search.SearchFolder;

/**
 * Search componenet that takes care of the searching for resources
 * 
 * @author bluecc
 *
 */
public class SearchView extends JPanel implements ContentSearcher, SearchPresenter {

  // Internal role
  private static final String DEFAULT_SELECTED_OPTION_SEARCH = "null";

  private static final Logger logger = Logger.getLogger(SearchView.class);
  /**
   * Objects interested in search events.
   */

  // Listeners of the search behavior
  private final transient List<SearchListener> listeners = new ArrayList<>();

  private JTextField searchField = null;
  private JButton searchButton = null;
  private JLabel filterLabel = null;

  // Option of the search (name, title)
  private String option = null;

  public SearchView() {
    String searchLabel = TranslationResourceController.getMessage("SEARCH_LABEL");
    String operationIsNotSupported = TranslationResourceController.getMessage("OPERATION_IS_NOT_SUPPORTED");

    setOpaque(true);

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Search JTextField constraints
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridwidth = 3;
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(1, 10, 1, 10);
    searchField = new JTextField(searchLabel);
    searchField.setOpaque(true);
    searchField.addFocusListener(new FocusListener() {

      @Override
      public void focusLost(FocusEvent e) {

        logger.debug(new UnsupportedOperationException(operationIsNotSupported));
      }

      @Override
      public void focusGained(FocusEvent e) {

        searchField.selectAll();
      }
    });
    searchField.validate();
    searchField.requestFocus();
    searchField.addKeyListener(new KeyListener() {

      @Override
      public void keyTyped(KeyEvent e) {

        logger.debug(new UnsupportedOperationException(operationIsNotSupported));
      }

      @Override
      public void keyReleased(KeyEvent e) {

        logger.debug(new UnsupportedOperationException(operationIsNotSupported));
      }

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          searchButton.doClick();
        }
      }
    });

    searchField.setEnabled(false);
    add(searchField, c);

    // Search JButton constraints
    c.gridwidth = 0;
    c.gridx = 3;
    c.gridy = 0;
    c.weightx = 0.0;

    URL filterIconUrl = getClass().getClassLoader().getResource("images/settings.png");
    filterLabel = new JLabel(new ImageIcon(filterIconUrl));
    filterLabel.addMouseListener(new FilterMouseHandler(this, searchField));
    filterLabel.setEnabled(false);

    searchButton = new JButton(searchLabel);
    searchButton.setEnabled(false);

    /**
     * This is where the fireSearch will occur when the button is pressed
     */
    searchButton.addActionListener(e -> {
      // The option will be orderer
      option = DEFAULT_SELECTED_OPTION_SEARCH;
      // Get the entered text and trim of white space from both sides
      final String searchText = searchField.getText().trim();

      doSearch(searchText, option, true);
    }

    );
    searchButton.setOpaque(true);

    add(filterLabel, c);
  }

  @Override
  public void doSearch(final String searchText, String option, boolean searchFolders) {
    // Get the search results of the query
    List<IResource> queryResults = searchItems(searchText, option, searchFolders);

    // Fire the search for each listener
    fireSearchFinished(searchText, queryResults, option, searchFolders);
  }

  /**
   * Searches async for each listener
   * 
   * @param searchText
   * @param queryResults
   */
  protected void fireSearchFinished(String searchText, List<IResource> queryResults, String option,
      boolean searchFolders) {
    for (SearchListener l : listeners) {
      l.searchFinished(searchText, queryResults, option, searchFolders);
    }
  }

  /**
   * Add from outside those listeners here to be used for search
   * 
   * @param searchListener
   */
  @Override
  public void addSearchListener(SearchListener searchListener) {
    listeners.add(searchListener);
  }

  /**
   * Add all the data searched in a list
   * 
   * @param searchText
   * @param option
   * @param searchFolders
   *          If should search also in folders. Default search will be for
   *          documents.
   * @return
   */
  private List<IResource> searchItems(String searchText, String option, boolean searchFolders) {
    List<IResource> queryResults = new ArrayList<>();
    SearchController searchCtrl = new SearchController(CMISAccess.getInstance().createResourceController());

    // The results from searching the documents
    ArrayList<IResource> documentsResults = (ArrayList<IResource>) new SearchDocument(searchText, searchCtrl, option)
        .getResultsFolder();

    for (IResource iResource : documentsResults) {
      logger.debug(" Doc id = " + iResource.getId());
      logger.debug(" Doc name = " + iResource.getDisplayName());
      logger.debug("\n");
    }

    logger.debug("Documents=" + documentsResults.size());

    queryResults.addAll(documentsResults);

    if (searchFolders) {
      // The results from searching the folders
      ArrayList<IResource> foldersResults = (ArrayList<IResource>) new SearchFolder(searchText, searchCtrl, option)
          .getResultsFolder();
      queryResults.addAll(foldersResults);
    }
    logger.debug("Results from server=" + queryResults.size());

    return queryResults;
  }

  /**
   * Find the line where the text was found
   */
  @Override
  public String getLineDoc(IResource doc, String matchPattern) {
    SearchController searchCtrl = new SearchController(CMISAccess.getInstance().clone().createResourceController());

    return searchCtrl.queryFindLine(doc, matchPattern);
  }

  @Override
  public String getPath(IResource doc, ResourceController ctrl) {

    return ((DocumentImpl) doc).getDocumentPath(ctrl);
  }

  @Override
  public void activateSearch() {
    filterLabel.setEnabled(true);
    searchField.setEnabled(true);
    searchButton.setEnabled(true);
  }

  @Override
  public String getProperties(IResource doc) {

    return ((DocumentImpl) doc).getDescription();
  }

  @Override
  public String getName(IResource resource) {
    return resource.getDisplayName();
  }

}
