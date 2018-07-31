package com.oxygenxml.cmis.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.oxygenxml.cmis.storage.SessionStorage;

public class ServerView extends JPanel implements ServerPresenter {

  private List<String> serversList;
  private JComboBox<String> serverItemsCombo;

  public ServerView(RepositoriesPresenter repoPresenter) {
    serverItemsCombo = new JComboBox<String>();
    this.serversList = new ArrayList<String>();
    /*
     * TESTING in comments
     * Arrays.assList has a fixed range no add allowed
     */
    // serversList.add("http://localhost:8080/B/atom11");
    // serversList.add("http://127.0.0.1:8098/alfresco/api/-default-/cmis/versions/1.1/atom");
    // serversList.add("http://localhost:8088/alfresco/api/-default-/cmis/versions/1.1/atom");
    // presentServers(serversList);

    // Add all new elements to the arraylist
    Collections.addAll(serversList, SessionStorage.getInstance().getSevers());

    // Present them
    presentServers(serversList);

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Server Url JLabel constraints
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.03;
    c.ipadx = 10;
    JLabel serverUrlLabel = new JLabel("Server URL:");
    add(serverUrlLabel, c);

    // Url http JComboBox constraints
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.87;
    c.gridwidth = 2;

    c.fill = GridBagConstraints.HORIZONTAL;

    serverItemsCombo.setEditable(true);
    serverItemsCombo.setEnabled(true);

    // Load JButton constraints constraints
    add(serverItemsCombo, c);
    c.gridx = 3;
    c.gridwidth = 1;
    c.gridy = 0;
    c.weightx = 0.1;
    JButton loadButton = new JButton("Connect");

    loadButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {

        // Try presentRepositories using the URL
        Object selectedItem = serverItemsCombo.getSelectedItem();
        if (selectedItem != null) {
          try {
            URL serverURL = new URL(serverItemsCombo.getSelectedItem().toString());
            System.out.println(serverItemsCombo.getEditor().getItem().toString().trim());
            serversList.add(serverItemsCombo.getEditor().getItem().toString().trim());

            repoPresenter.presentRepositories(serverURL);

            // TODO What is this thing toArray( new String[0] )
             SessionStorage.getInstance().setServers(serversList.toArray(new
             String[0]));

          } catch (MalformedURLException e1) {
            JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
          }
        }

      }
    });
    add(loadButton, c);
  }

  @Override
  public void presentServers(List<String> serversList) {

    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    // Iterate all the elements
    for (String element : serversList) {
      model.addElement(element);
    }

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
