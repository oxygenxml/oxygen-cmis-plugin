package com.oxygenxml.cmis.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.plugin.TranslationResourceController;
import com.oxygenxml.cmis.storage.SessionStorage;

/**
 * Server component that takes care of the behavior of the servers, how are
 * entered,chosen.
 * 
 * @author bluecc
 *
 */
public class ServerView extends JPanel {

  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(ServerView.class);

  // Order shall be saved
  private final Set<String> serversList = new LinkedHashSet<>();
  // Combo box to choose from in-memory servers
  private final JComboBox<String> serverItemsCombo = new JComboBox<>();
  private final JButton loadButton;

  /**
   * Constructor that creates the component
   * 
   * @param repoPresenter
   * @exception MalformedURLException
   */
  public ServerView(RepositoriesPresenter repoPresenter, SearchPresenter searchPresenter) {
    // Elements constants
    String operationIsNotSupported = TranslationResourceController.getMessage("OPERATION_IS_NOT_SUPPORTED");
    String serverUrlLabelValue = TranslationResourceController.getMessage("SERVER_URL_LABEL") + ":";
    String connectButtonValue = TranslationResourceController.getMessage("CONNECT_BUTTON");

    /*
     * TESTING in comments Arrays.assList has a fixed range no add allowed
     */

    // "http://localhost:8088/alfresco/api/-default-/cmis/versions/1.1/atom
    // http://localhost:8990/alfresco/api/-default-/cmis/versions/1.1/atom

    // Add all new elements to the LinkedHash set (unique and ordered)
    Set<String> elements = SessionStorage.getInstance().getSevers();

    if (elements != null) {
      Collections.addAll(serversList, elements.toArray(new String[0]));
    }
    setOpaque(true);
    // Present them
    presentServers(serversList);

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Server Url JLabel constraints
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(1, 10, 1, 10);
    c.fill = GridBagConstraints.NONE;
    JLabel serverUrlLabel = new JLabel(serverUrlLabelValue);

    serverUrlLabel.setOpaque(true);

    add(serverUrlLabel, c);

    // Url http JComboBox constraints
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1.0;
    c.gridwidth = 2;

    c.fill = GridBagConstraints.HORIZONTAL;

    serverItemsCombo.setEditable(true);
    serverItemsCombo.setEnabled(true);
    serverItemsCombo.setOpaque(true);
    serverItemsCombo.addFocusListener(new FocusListener() {

      @Override
      public void focusLost(FocusEvent e) {
        logger.debug(new UnsupportedOperationException(operationIsNotSupported));

      }

      @Override
      public void focusGained(FocusEvent e) {
        serverItemsCombo.setFocusable(true);

      }
    });
    serverItemsCombo.validate();
    serverItemsCombo.requestFocus();

    serverItemsCombo.getEditor().getEditorComponent().addKeyListener(new KeyListener() {

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
          loadButton.doClick();
        }
      }
    });

    // Load JButton constraints constraints
    add(serverItemsCombo, c);
    c.gridx = 3;
    c.gridwidth = 1;
    c.gridy = 0;
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    loadButton = new JButton(connectButtonValue);

    loadButton.addActionListener(e -> {

      // Try presentRepositories using the URL
      Object selectedItem = serverItemsCombo.getSelectedItem();

      if (selectedItem != null) {
        try {
          // Get the url from the item selected
          URL serverURL = new URL(serverItemsCombo.getSelectedItem().toString().trim());

          // Get the typed URL and trim it
          String currentServerURL = serverItemsCombo.getEditor().getItem().toString().trim();

          // Add to the serves list
          serversList.add(currentServerURL);
          // Present repositories
          presentRepositories(repoPresenter, searchPresenter, serverURL);

          // Add the server
          SessionStorage.getInstance().addServer(currentServerURL);

        } catch (MalformedURLException e1) {

          // Show an exception if there is one
          logger.debug("Exception ", e1);
        }
      }

    });
    loadButton.setOpaque(true);

    add(loadButton, c);
  }

  /**
   * Tries to present repositories and activate the search
   * 
   * @param repoPresenter
   * @param searchPresenter
   * @param serverURL
   */
  private void presentRepositories(RepositoriesPresenter repoPresenter, SearchPresenter searchPresenter,
      URL serverURL) {
    try {

      repoPresenter.presentRepositories(serverURL);
      searchPresenter.activateSearch();

    } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException ev) {

      // Show an exception if there is one
      logger.debug("Exception ", ev);
    }
  }

  /**
   * Present the servers using a model for rendering
   * 
   * 
   * @param serversList
   */
  public void presentServers(Set<String> serversList) {
    // Create the model
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

    // Iterate all the elements
    for (String element : serversList) {
      model.addElement(element);
    }

    // Set the model
    serverItemsCombo.setModel(model);

    /*
     * Render all the elements of the listRepo
     */
    serverItemsCombo.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
          boolean cellHasFocus) {

        String renderText = value.toString();

        return super.getListCellRendererComponent(list, renderText, index, isSelected, cellHasFocus);
      }
    });

  }

}
