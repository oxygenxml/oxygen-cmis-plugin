package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.chemistry.opencmis.client.api.Repository;

import com.oxygenxml.cmis.core.CMISAccess;

public class RepoComboBoxView extends JPanel implements RepositoriesPresenter {

  private JComboBox<Repository> repoItems;
  private List<Repository> serverReposList;
  private ItemsPresenter itemsPresenter;
  private URL serverURL;

  RepoComboBoxView(ItemsPresenter itemsPresenter) {
    // Initialize the itemsPresenter
    this.itemsPresenter = itemsPresenter;
    repoItems = new JComboBox<Repository>();

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Server Repository JLabel constraints
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.02;
    JLabel serverUrlLabel = new JLabel("Repository:");
    add(serverUrlLabel, c);

    // Url http JComboBox constraints
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.9;
    c.gridwidth = 2;
    c.ipadx = 40;
    c.fill = GridBagConstraints.HORIZONTAL;

    repoItems.setEditable(false);
    add(repoItems, c);

    repoItems.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        JComboBox comboBox = (JComboBox) e.getSource();

        Repository selected = (Repository) comboBox.getSelectedItem();
        System.out.println(selected.getId());
        itemsPresenter.presentItems(serverURL, selected.getId());

      }
    });
  }

  /*
   * Implement presentRepositories using the serverURL
   */
  @Override
  public void presentRepositories(URL serverURL) {
    this.serverURL = serverURL;

    // Create the listRepo of repos.
    System.out.println(serverURL);
    serverReposList = CMISAccess.getInstance().getRepositories(serverURL);

    DefaultComboBoxModel<Repository> model = new DefaultComboBoxModel<>();
    // Iterate all the elements
    for (Repository element : serverReposList) {
      model.addElement(element);
    }

    repoItems.setModel(model);

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
        }

        return super.getListCellRendererComponent(list, renderTex, index, isSelected, cellHasFocus);
      }
    });

  }
}
