package com.oxygenxml.cmis.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.oxygenxml.cmis.storage.SessionStorage;

/**
 * Server component that takes care of the behavior of the servers, how are
 * entered,chosen.
 * 
 * @author bluecc
 *
 */
public class ServerView extends JPanel {
  // Order shall be savedG
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
    /*
     * TESTING in comments Arrays.assList has a fixed range no add allowed
     */
    // serversList.add("http://localhost:8080/B/atom11");
    // serversList.add("http://127.0.0.1:8098/alfresco/api/-default-/cmis/versions/1.1/atom");
    // serversList.add("http://localhost:8088/alfresco/api/-default-/cmis/versions/1.1/atom");
    // serversList.add("http://localhost:8990/alfresco/api/-default-/cmis/versions/1.1/atom");
    // presentServers(serversList);

    // Add all new elements to the LinkedHash set (unique and ordered)
    Set<String> elements = SessionStorage.getInstance().getSevers();

    if (elements != null) {
      Collections.addAll(serversList, elements.toArray(new String[0]));
    }
    setOpaque(true);
    // setBackground(Color.CYAN);
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
    JLabel serverUrlLabel = new JLabel("Server URL:");

    serverUrlLabel.setOpaque(true);
    // serverUrlLabel.setBackground(Color.red);

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
        // TODO Auto-generated method stub

      }

      @Override
      public void focusGained(FocusEvent e) {
        // TODO Auto-generated method stub
        serverItemsCombo.setFocusable(true);
        ;
      }
    });
    serverItemsCombo.validate();
    serverItemsCombo.requestFocus();

    serverItemsCombo.getEditor().getEditorComponent().addKeyListener(new KeyListener() {

      @Override
      public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

      }

      @Override
      public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

      }

      @Override
      public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          loadButton.doClick();
        }
      }
    });
    // serverItemsCombo.setBackground(Color.yellow);
    // Load JButton constraints constraints
    add(serverItemsCombo, c);
    c.gridx = 3;
    c.gridwidth = 1;
    c.gridy = 0;
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    loadButton = new JButton("Connect");

    loadButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {

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
            try {

              repoPresenter.presentRepositories(serverURL);
              searchPresenter.activateSearch();

            } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException ev) {

              // Show an exception if there is one
              JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
            }
            // Add the server
            SessionStorage.getInstance().addServer(currentServerURL);

          } catch (MalformedURLException e1) {

            // Show an exception if there is one
            JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
          }
        }

      }
    });
    loadButton.setOpaque(true);
    // loadButton.setBackground(new Color(0, 0, 255));
    add(loadButton, c);
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
