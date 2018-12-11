package com.oxygenxml.cmis.ui.test;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import com.oxygenxml.cmis.ui.ControlComponents;
import com.oxygenxml.cmis.ui.RepoComboBoxView;
import com.oxygenxml.cmis.ui.SearchView;
import com.oxygenxml.cmis.ui.ServerView;
/**
 * Main frame of the desktop version that serves also the TabComponentsView
 * @author bluecc
 *
 */
public class RunPlugin extends JFrame {
  
  /*
   * northPanel shall wrap the serverPanel and searchPanel
   */
  JPanel northPanel;
  ServerView serverPanel;
  SearchView searchPanel;
  RepoComboBoxView repoPanel;
  /*
   * splitPaneBottom is the Tab section from the bottom
   */
  TabComponentsView splitPaneBottom;
  
  public RunPlugin() {
    super("runPlugin");


    /* 
     * Create the bottom of the separator
     */
    TabComponentsView bottomPanel = new TabComponentsView();
    
    /*
     * Create the top of the separator that includes the itemList and repoList
     * 
     *  TODO This test dialog is no longer bound to a document presenter.
     */
    ControlComponents topPanel = new ControlComponents();


    // Create the splitPanel from center
    JSplitPane centerPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
    centerPanel.setOneTouchExpandable(true);
    centerPanel.setDividerLocation(300);
    centerPanel.setContinuousLayout(true);

    getContentPane().add(centerPanel, BorderLayout.CENTER);

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

  public static void main(String[] args) throws Exception {
    
    UIManager.setLookAndFeel(
        UIManager.getSystemLookAndFeelClassName());
    
    // Schedule a job for the event-dispatching thread:
    // creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        createAndShowGUI();
      }
    });
  }

}
