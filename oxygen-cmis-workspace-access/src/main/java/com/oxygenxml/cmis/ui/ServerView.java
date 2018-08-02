package com.oxygenxml.cmis.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.oxygenxml.cmis.storage.SessionStorage;

public class ServerView extends JPanel {

  private Set<String> serversList = new LinkedHashSet<>();
  private JComboBox<String> serverItemsCombo = new JComboBox<>();

  public ServerView(RepositoriesPresenter repoPresenter) {
    /*
     * TESTING in comments Arrays.assList has a fixed range no add allowed
     */
    // serversList.add("http://localhost:8080/B/atom11");
    // serversList.add("http://127.0.0.1:8098/alfresco/api/-default-/cmis/versions/1.1/atom");
    // serversList.add("http://localhost:8088/alfresco/api/-default-/cmis/versions/1.1/atom");
    // presentServers(serversList);
    

    // Add all new elements to the LinkedHash set (unique and ordered)
    Set<String> elements = SessionStorage.getInstance().getSevers();
    
    if (elements != null) {
      Collections.addAll(serversList, elements.toArray(new String[0]));
    }
    
    // Present them
    presentServers(serversList);

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Server Url JLabel constraints
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.ipadx = 10;
    c.insets = new Insets(1,5,1,5);
    c.fill = GridBagConstraints.NONE;
    JLabel serverUrlLabel = new JLabel("Server URL:");
    add(serverUrlLabel, c);

    // Url http JComboBox constraints
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1.0;
    c.gridwidth = 2;

    c.fill = GridBagConstraints.HORIZONTAL;

    serverItemsCombo.setEditable(true);
    serverItemsCombo.setEnabled(true);

    // Load JButton constraints constraints
    add(serverItemsCombo, c);
    c.gridx = 3;
    c.gridwidth = 1;
    c.gridy = 0;
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    JButton loadButton = new JButton("Connect");

    loadButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {

        // Try presentRepositories using the URL
        Object selectedItem = serverItemsCombo.getSelectedItem();

        if (selectedItem != null) {
          try {
            URL serverURL = new URL(serverItemsCombo.getSelectedItem().toString());

            String currentServerURL = serverItemsCombo.getEditor().getItem().toString().trim();
            serversList.add(currentServerURL);

            repoPresenter.presentRepositories(serverURL);

            // Set the servers
            SessionStorage.getInstance().addServer(currentServerURL);
          } catch (MalformedURLException e1) {
            JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
          }
        }

      }
    });
    add(loadButton, c);
  }

  public void presentServers(Set<String> serversList) {

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
