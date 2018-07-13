package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class RunPlugin extends JFrame {
  JPanel northPanel;
  ServerView serverPanel;
  SearchView searchPanel;
  TabComponentsView splitPaneBottom;
  
  public RunPlugin() {
    super("runPlugin");

    /*
     * Creation of the centerPanel
     */

    // Create the bottom of the separator
    TabComponentsView splitPaneBottom = new TabComponentsView();
    
    // Create the top of the separator
    SplitPaneTop splitPaneDemo = new SplitPaneTop(splitPaneBottom);
    
    JSplitPane splitPaneTop = splitPaneDemo.getSplitPane();
    splitPaneTop.setBorder(null);
    splitPaneTop.setContinuousLayout(true);
    splitPaneTop.setMinimumSize(new Dimension(200, 100));


    // Create the splitPanel from center
    JSplitPane splitPaneCenter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneTop, splitPaneBottom);

    // config for the splitpane
    splitPaneCenter.setOneTouchExpandable(true);
    splitPaneCenter.setDividerLocation(180);
    splitPaneCenter.setContinuousLayout(true);


    /*
     * Creation of the northPanel
     */
    // Creation of the northern grid

    northPanel = new JPanel();
    northPanel.setLayout(new GridLayout(2,1));
    serverPanel = new ServerView(splitPaneDemo.getRepositoriesPresenter());
    searchPanel = new SearchView(splitPaneDemo.getItemsPresenter());
    
    
    northPanel.add(serverPanel);
    northPanel.add(searchPanel);
    


    // Add the northpanel to this frame
    getContentPane().add(northPanel, BorderLayout.NORTH);
    // Add the centerPanel to this frame
    getContentPane().add(splitPaneCenter, BorderLayout.CENTER);

  }

  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event-dispatching thread.
   */
  private static void createAndShowGUI() {

    // Create and set up the window.
    JFrame frame = new RunPlugin();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Display the window.
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setSize(600, 500);
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    // Schedule a job for the event-dispatching thread:
    // creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }

}
