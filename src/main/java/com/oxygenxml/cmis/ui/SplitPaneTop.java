package com.oxygenxml.cmis.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class SplitPaneTop extends JPanel implements ListSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel itemRepo;
	private JList listRepo;
	private JSplitPane splitPane;
	private String[] repoNames = { "Repo1", "Repo2","Repo1", "Repo2","Repo1", "Repo2","Repo1", "Repo2","Repo1", "Repo2" };

	public SplitPaneTop() {

		// Create the listRepo of repos.
		listRepo = new JList(repoNames);
		listRepo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listRepo.setSelectedIndex(0);
		listRepo.addListSelectionListener(this);

		// Scroller for the listRepo
		JScrollPane listRepoScrollPane = new JScrollPane(listRepo);
		itemRepo = new JLabel();
		itemRepo.setFont(itemRepo.getFont().deriveFont(Font.ITALIC));
		itemRepo.setHorizontalAlignment(JLabel.CENTER);

		// Scroller for the items from Repo
		JScrollPane itemRepoScrollPane = new JScrollPane(itemRepo);

		// Create a split pane with the listRepoScrollPane and itemRepoScrollPane.
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listRepoScrollPane, itemRepoScrollPane);

		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);

		// Provide minimum size for the sidelist
		Dimension minimumSizeSideList = new Dimension(200, 100);
		// Provide minmum size for the mainlist
		Dimension minimumSizeMainList = new Dimension(400, 100);

		// Setting the minum size for the SideList and MainList
		listRepoScrollPane.setMinimumSize(minimumSizeSideList);
		itemRepoScrollPane.setMinimumSize(minimumSizeMainList);

		// Provide a preferred size for the split pane.
		splitPane.setPreferredSize(new Dimension(400, 400));
		// splitPane.isContinuousLayout();
		updateLabel(repoNames[listRepo.getSelectedIndex()]);

	}

	// Listens to the listRepo
	public void valueChanged(ListSelectionEvent e) {
		JList listRepo = (JList) e.getSource();
		updateLabel(repoNames[listRepo.getSelectedIndex()]);
	}

	// Renders the selected image
	protected void updateLabel(String name) {
		ImageIcon icon = createImageIcon("images/" + name + ".gif");
		itemRepo.setIcon(icon);
		if (icon != null) {
			itemRepo.setText(null);
		} else {
			itemRepo.setText("Item not found");
		}
	}

	// Used by SplitPaneTop
	public JList getImageList() {
		return listRepo;
	}

	public JSplitPane getSplitPane() {
		return splitPane;
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = SplitPaneTop.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

}
