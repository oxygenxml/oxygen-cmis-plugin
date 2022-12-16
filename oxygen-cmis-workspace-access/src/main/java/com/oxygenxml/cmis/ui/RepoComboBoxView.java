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
import javax.swing.JPanel;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.CmisAccessSingleton;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.plugin.Tags;
import com.oxygenxml.cmis.plugin.TranslationResourceController;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

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
    repositoryLabel = TranslationResourceController.getMessage(Tags.REPOSITORY_LABEL) + ":";
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
    repoItems.setRenderer(new RepositoryRenderer());
  }

  /**
   * On changed repository reset the breadcrumb and present the resources again
   * of the current ropsitory.
   * 
   * @param serverURL
   * @param repositoryID
   */
  protected void fireRepositoryChangedEvent(URL serverURL, String repositoryID) {
    if (logger.isDebugEnabled()) {
      logger.debug("Notify repository changed event. Server url: " + serverURL + ", repository id: "  + repositoryID);
    }
    
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

    // Check credentials for the URL
    UserCredentials userCredentials = null;

    List<Repository> serverReposList = null;
    // Check if is logged in and there repositories to present
    boolean connected = false;
    try {
      do {
        userCredentials = AuthenticatorUtil.getUserCredentials(serverURL);

        // Create the listRepo of repos.
        if (logger.isDebugEnabled()) {
          logger.debug("Load repositories from: " + serverURL + " credentials: " + userCredentials);
        }

        try {
          // Get the repositories
          serverReposList = CmisAccessSingleton.getInstance().connectToServerGetRepositories(serverURL, userCredentials);
          
          connected = true;
        } catch (CmisUnauthorizedException e) {
          // Will try again.
          if (logger.isDebugEnabled()) {
            logger.debug(e.getMessage(), e);
          }
        }
      } while (!connected);

      if (logger.isDebugEnabled()) {
        logger.debug(serverReposList + "repos");
      }

      // If there some put them in the model to be shown
      if (serverReposList != null && !serverReposList.isEmpty()) {
        DefaultComboBoxModel<Repository> model = new DefaultComboBoxModel<>();
        // Iterate all the elements
        for (Repository element : serverReposList) {
          model.addElement(element);
        }
        repoItems.setModel(model);

        fireRepositoryChangedEvent(serverURL, serverReposList.get(0).getId());
      }
    } catch (UserCanceledException e) {
      if (logger.isDebugEnabled()) {
        logger.debug(e.getMessage(), e);
      }
    } catch (CmisRuntimeException e) {
      // Unexpected exception
      logger.debug(e.getMessage(), e);
      
      PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage("Unable to retrieve repositories because of: " + e.getMessage(), e);
    } 
  }
  

  /**
   * Renders the name or the ID for a repository.
   */
  private final class RepositoryRenderer extends DefaultListCellRenderer {
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
  }
}
