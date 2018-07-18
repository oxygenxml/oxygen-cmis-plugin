package com.oxygenxml.cmis.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ServerView extends JPanel {

  private RepositoriesPresenter repoPresenter;

  public ServerView(RepositoriesPresenter repoPresenter) {
    // TODO Get all the cached servers 
    String servers[]={"http://localhost:8080/B/atom11","http://lets-talk.com"};
    
    this.repoPresenter = repoPresenter;
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Server Url JLabel constraints
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.02;
    JLabel serverUrlLabel = new JLabel(" Server URL: ");
    add(serverUrlLabel, c);

    // Url http JComboBox constraints
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.9;
    c.gridwidth = 2;
    c.ipadx = 40;
    c.fill = GridBagConstraints.HORIZONTAL;
    
    JComboBox<String>urlComboBox=new JComboBox<String>(servers);    
    urlComboBox.setEditable(true);
    urlComboBox.setEnabled(true);
    
    // Load JButton constraints constraints
    add(urlComboBox, c);
    c.gridx = 3;
    c.gridwidth = 1;
    c.gridy = 0;
    c.weightx = 0.08;
    JButton loadButton = new JButton("Connect");

    loadButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        
        // Try presentRepositories using the URL
        try {
          System.out.println("Load triggered="+urlComboBox.getItemAt(urlComboBox.getSelectedIndex()));
          repoPresenter.presentRepositories(new URL(urlComboBox.getItemAt(urlComboBox.getSelectedIndex())));
        } catch (MalformedURLException e1) {

          e1.printStackTrace();
        }
      }
    });
    add(loadButton, c);
  }

}
