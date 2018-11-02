package com.oxygenxml.cmis.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.plugin.TranslationResourceController;

/**
 * Describes how the repositories a shown and their behaviors
 * 
 * @author bluecc
 */
public class RepoComboBoxView extends JPanel implements RepositoriesPresenter {

  private final String repositoryLabel;
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(RepoComboBoxView.class);

  // Items to be shown
  private final JComboBox<Repository> repoItems;
  /**
   * Parties interested about the repository change events.
   */
  private final transient List<RepositoryListener> listeners = new ArrayList<>();

  private URL serverURL;

  /**
   * Constructor that receives an items presenter to show the items inside the
   * server and update the breadcrumb with breadcrumbPresenter
   * 
   * Creates the repositories component visually
   */
  public RepoComboBoxView() {
    repositoryLabel = TranslationResourceController.getMessage("REPOSITORY_LABEL") + ":";
    setOpaque(true);

    repoItems = new JComboBox<>();

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Server Repository JLabel constraints
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(1, 10, 1, 10);
    JLabel serverUrlLabel = new JLabel(repositoryLabel);
    serverUrlLabel.setOpaque(true);
    add(serverUrlLabel, c);

    // Url http JComboBox constraints
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    repoItems.setOpaque(true);

    repoItems.setEnabled(false);
    repoItems.setEditable(false);

    add(repoItems, c);
    /**
     * Gets the current selected url from combo box
     */
    repoItems.addActionListener(e -> {
      JComboBox<?> comboBox = (JComboBox<?>) e.getSource();

      Repository selected = (Repository) comboBox.getSelectedItem();
      if (logger.isDebugEnabled()) {
        logger.debug(selected.getId());
      }

      fireRepositoryChangedEvent(serverURL, selected.getId());
    });

    /*
     * Render all the elements of the listRepo
     */
    repoItems.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
          boolean cellHasFocus) {
        String renderTex = "";
        if (value != null) {
          renderTex = ((Repository) value).getName();

          if (renderTex.length() == 0) {
            renderTex = ((Repository) value).getId();
          }
        }

        return super.getListCellRendererComponent(list, renderTex, index, isSelected, cellHasFocus);
      }
    });
  }

  /**
   * On changed repository reset the breadcrumb and present the resources again
   * of the current ropsitory.
   * 
   * @param serverURL
   * @param repositoryID
   */
  protected void fireRepositoryChangedEvent(URL serverURL, String repositoryID) {
    for (RepositoryListener repositoryListener : listeners) {
      repositoryListener.repositoryConnected(serverURL, repositoryID);
    }
  }

  /**
   * Adds repositories to the list to be reseted
   * 
   * @param repositoryListener
   */
  public void addRepositoryListener(RepositoryListener repositoryListener) {
    listeners.add(repositoryListener);
  }

  /**
   * Implement presentRepositories using the serverURL
   * 
   * @exception Exception
   */
  @Override
  public void presentRepositories(URL serverURL) {
    this.serverURL = serverURL;
    repoItems.setEnabled(true);

    // Create the listRepo of repos.
    if (logger.isDebugEnabled()) {
      logger.debug("Load repositories from: " + serverURL);
    }

    // Check credentials for the URL
    UserCredentials userCredentials = null;
    try {
      userCredentials = AuthenticatorUtil.getUserCredentials(serverURL);

    } catch (Exception e1) {
      logger.error(e1, e1);

      JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
    }

    if (userCredentials != null) {
      boolean loggedin = false;

      List<Repository> serverReposList = null;
      // Check if is logged in and there repositories to present
      do {

        // Check if logged in
        loggedin = AuthenticatorUtil.isLoggedin(serverURL);

        // Get the repositories
        serverReposList = CMISAccess.getInstance().connectToServerGetRepositories(serverURL, userCredentials);

      } while (serverReposList == null && !loggedin);

      if (logger.isDebugEnabled()) {
        logger.debug(serverReposList + "repos");
      }

      // If there some put them in the model to be shown
      if (serverReposList != null && !serverReposList.isEmpty()) {

        fireRepositoryChangedEvent(serverURL, serverReposList.get(0).getId());

        DefaultComboBoxModel<Repository> model = new DefaultComboBoxModel<>();
        // Iterate all the elements
        for (Repository element : serverReposList) {
          model.addElement(element);
        }

        repoItems.setModel(model);
      }
    }
  }
}
