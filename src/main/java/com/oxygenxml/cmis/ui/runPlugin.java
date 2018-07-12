package com.oxygenxml.cmis.ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
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

public class runPlugin extends JFrame implements ListSelectionListener {
	private JLabel label;

	public runPlugin() {
		super("runPlugin");

		/*
		 * Creation of the centerPanel
		 */
		// Create the top of the separator
		SplitPaneTop splitPaneDemo = new SplitPaneTop();
		JSplitPane splitPaneTop = splitPaneDemo.getSplitPane();

		splitPaneDemo.getImageList().addListSelectionListener(this);

		splitPaneTop.setBorder(null);
		splitPaneTop.setContinuousLayout(true);

		// Create the bottom of the separator
		TabComponentsDemo splitPaneBottom = new TabComponentsDemo();
		splitPaneBottom.setLayout(new GridLayout(0, 1));
		splitPaneBottom.setVisible(true);

		// Create the splitPanel from center
		JSplitPane splitPaneCenter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneTop, splitPaneBottom);

		// config for the splitpane
		splitPaneCenter.setOneTouchExpandable(true);
		splitPaneCenter.setDividerLocation(180);
		splitPaneCenter.setContinuousLayout(true);

		// Provide minimum sizes for the two components in the split pane
		// Dimension class
		splitPaneTop.setMinimumSize(new Dimension(200, 100));
		splitPaneBottom.setMinimumSize(new Dimension(200, 100));

		/*
		 * Creation of the northPanel
		 */
		// Creation of the northern grid
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// Server Url JLabel constraints
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.02;
		JLabel serverUrlLabel = new JLabel("Server URL:");
		northPanel.add(serverUrlLabel, c);
		 
		// Url http JTextField constraints
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.9;
		c.gridwidth = 2;
		c.ipadx = 40;
		c.fill = GridBagConstraints.HORIZONTAL;
		JTextField serverUrlField = new JTextField("http://");

		//Load JButton constraints constraints
		northPanel.add(serverUrlField, c);
		c.gridx = 3;
		c.gridwidth = 1;
		c.gridy = 0;
		c.weightx = 0.08;
		JButton loadButton = new JButton("Load");
		northPanel.add(loadButton, c);

		// Search JTextField constraints
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.9;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		c.ipadx = 40;
		JTextField searchField = new JTextField("Search");
		northPanel.add(searchField, c);
		
		// Search JButton constraints
		c.gridwidth = 0;
		c.gridx = 3;
		c.gridy = 1;
		c.weightx = 0.1;

		JButton searchButton = new JButton("Search");
		northPanel.add(searchButton, c);

		/*
		 * Creation of the southPanel
		 */

		// Add the northpanel to this frame
		getContentPane().add(northPanel, BorderLayout.NORTH);
		// Add the centerPanel to this frame
		getContentPane().add(splitPaneCenter, BorderLayout.CENTER);

	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;
		
		// Not working
		// JList theList = (JList) e.getSource();
		// if (theList.isSelectionEmpty()) {
		// label.setText("Nothing selected.");
		// } else {
		// int index = theList.getSelectedIndex();
		// label.setText("Selected image number " + index);
		// }
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		
		// Create and set up the window.
		JFrame frame = new runPlugin();
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
