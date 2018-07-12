package com.oxygenxml.cmis.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class TabComponentsDemo extends JPanel {

	private final int tabNumber = 5;
	private final JTabbedPane pane = new JTabbedPane();

	/*
	 * Initialize the components
	 */
	public TabComponentsDemo() {
		runTest();
		add(pane);

	}

	/*
	 * Add the tab names and initialize each component
	 */
	public void runTest() {
		pane.removeAll();
		for (int i = 0; i < tabNumber; i++) {
			String title = "Tab " + i;
			pane.add(title, new JLabel(title));
			initTabComponent(i);
		}
		pane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
	}

	/*
	 * Initialize each component using ButtonTabComponent class
	 */
	private void initTabComponent(int i) {
		pane.setTabComponentAt(i, new ButtonTabComponent(pane));
	}

}