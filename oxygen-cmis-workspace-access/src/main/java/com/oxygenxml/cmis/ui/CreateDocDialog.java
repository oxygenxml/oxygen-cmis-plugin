package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;

public class CreateDocDialog extends OKCancelDialog {
  private static JFrame frame;
  private CreateDocInputPanel inputPanel;

  public CreateDocDialog() {
    super(frame, "Create document", true);
    frame = (JFrame) PluginWorkspaceProvider.getPluginWorkspace().getParentFrame();

    // Get the parent container
    Container cont = getContentPane();
    inputPanel = new CreateDocInputPanel();

    cont.setLayout(new BorderLayout());
    cont.add(inputPanel, BorderLayout.CENTER);
    // Show it in the center of the frame
    setLocationRelativeTo(frame);
    setModal(true);
  }

  // TODO: I doubt this decision
  public String getVersioningState() {
    return inputPanel.getVersioningState();
  }
}

class CreateDocInputPanel extends JPanel implements ActionListener {
  private JLabel versionLabel;
  private JRadioButton radioItemMajor;
  private JRadioButton radioItemMinor;
  private JRadioButton radioItemNone;
  private JPanel radioPanel;

  private String versioningState;

  public CreateDocInputPanel() {
    radioPanel = new JPanel(new GridLayout(1, 3));
    versionLabel = new JLabel("Enter the file name and the extension: ");

    setLayout(new GridLayout(1, 2, 0, 0));
    add(versionLabel);

    // MAJOR
    radioItemMajor = new JRadioButton("Major");
    radioItemMajor.setActionCommand("MAJOR");
    radioItemMajor.addActionListener(this);
    // Set selected
    radioItemMajor.setSelected(true);
    versioningState = "MAJOR";

    // MINOR
    radioItemMinor = new JRadioButton("Minor");
    radioItemMinor.setActionCommand("MINOR");
    radioItemMinor.addActionListener(this);

    // NONE
    radioItemNone = new JRadioButton("None");
    radioItemNone.setActionCommand("NONE");

    radioItemNone.addActionListener(this);
    radioPanel.add(radioItemMajor);
    radioPanel.add(radioItemMinor);
    radioPanel.add(radioItemNone);
    add(radioPanel);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equals("MAJOR")) {
      radioItemMinor.setSelected(false);
      radioItemNone.setSelected(false);
      versioningState = e.getActionCommand();
    }

    if (e.getActionCommand().equals("MINOR")) {
      radioItemMajor.setSelected(false);
      radioItemNone.setSelected(false);
      versioningState = e.getActionCommand();
    }
    if (e.getActionCommand().equals("NONE")) {
      radioItemMajor.setSelected(false);
      radioItemMinor.setSelected(false);
      versioningState = e.getActionCommand();
    }
  }

  public String getVersioningState() {
    return this.versioningState;
  }
}
