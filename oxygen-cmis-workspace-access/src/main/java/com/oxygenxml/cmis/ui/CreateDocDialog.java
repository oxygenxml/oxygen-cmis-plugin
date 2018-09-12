package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.batik.ext.swing.GridBagConstants;

import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;

public class CreateDocDialog extends OKCancelDialog {
  private CreateDocInputPanel inputPanel;

  public CreateDocDialog(JFrame frame) {
    super(frame, "Create document", true);

    // Get the parent container
    Container cont = getContentPane();
    inputPanel = new CreateDocInputPanel();

    cont.add(inputPanel, BorderLayout.CENTER);

    getOkButton().setText("Create");

    // Show it in the center of the frame
    setLocationRelativeTo(frame);
    setResizable(true);
    setModal(true);
    pack();
    setVisible(true);
  }


  // TODO: I doubt this decision
  public String getFileName() {
    return inputPanel.getFileName();
  }

  // TODO: I doubt this decision
  public String getVersioningState() {
    return inputPanel.getVersioningState();
  }
}

class CreateDocInputPanel extends JPanel implements ActionListener {
  private JLabel messageLabel, versionLabel;
  private JTextField filename;
  private JRadioButton radioItemMajor;
  private JRadioButton radioItemMinor;
  private JRadioButton radioItemNone;

  private String versioningState;

  public CreateDocInputPanel() {
    setLayout(new GridBagLayout());

    messageLabel = new JLabel("File name:");
    // messageLabel.setOpaque(true);
    // messageLabel.setBackground(Color.BLUE);
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    // c.weightx = 0;
    // c.ipadx = 10;
    c.gridwidth = 1;
    c.insets = new Insets(0, 5, 3, 5);
    c.fill = GridBagConstants.BOTH;
    add(messageLabel, c);

    filename = new JTextField("myfile.txt");
    // filename.setOpaque(true);
    // filename.setBackground(Color.RED);
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 3;
    c.weightx = 1;
    // c.ipadx = 10;
    // c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstants.BOTH;
    add(filename, c);

    versionLabel = new JLabel("Version:");
    // versionLabel.setOpaque(true);
    // versionLabel.setBackground(Color.BLUE);
    c.gridx = 0;
    c.gridy = 2;
    c.anchor = GridBagConstants.WEST;
    c.gridwidth = 1;
    c.weightx = 0;
    // c.ipadx = 10;
    // c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstants.VERTICAL;
    add(versionLabel, c);

    // MAJOR
    radioItemMajor = new JRadioButton("Major");
    // radioItemMajor.setOpaque(true);
    // radioItemMajor.setBackground(Color.RED);
    radioItemMajor.setActionCommand("MAJOR");
    radioItemMajor.addActionListener(this);
    // Set selected
    radioItemMajor.setSelected(true);
    versioningState = "MAJOR";
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 1;
    c.anchor = GridBagConstants.CENTER;
    c.weightx = 0;
    // c.ipadx = 10;
    // c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstants.BOTH;
    add(radioItemMajor, c);

    // MINOR
    radioItemMinor = new JRadioButton("Minor");
    // radioItemMinor.setOpaque(true);
    // radioItemMinor.setBackground(Color.yellow);
    radioItemMinor.setActionCommand("MINOR");
    radioItemMinor.addActionListener(this);
    c.anchor = GridBagConstants.CENTER;
    c.gridx = 2;
    c.gridy = 2;
    c.gridwidth = 1;
    c.weightx = 0;
    // c.ipadx = 10;
    // c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstants.BOTH;
    add(radioItemMinor, c);

    // NONE
    radioItemNone = new JRadioButton("None");
//    radioItemNone.setOpaque(true);
//    radioItemNone.setBackground(Color.GREEN);
    radioItemNone.setActionCommand("NONE");
    radioItemNone.addActionListener(this);
    c.anchor = GridBagConstants.CENTER;
    c.gridx = 3;
    c.gridy = 2;
    c.gridwidth = 1;
    c.weightx = 0;
    // c.ipadx = 10;
    // c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstants.BOTH;
    add(radioItemNone, c);

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

  public String getFileName() {
    return this.filename.getText();
  }
}
