package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
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
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.batik.ext.swing.GridBagConstants;

import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;

/**
 * Dialog thats extends a dialog with cancel and ok button and uses a main panel
 * CheckinInputPanel
 * 
 * @author bluecc
 *
 */
public class CheckinDocDialog extends OKCancelDialog {
  // Main inputPanel
  private CheckinInputPanel inputPanel;

  /**
   * The constructor that uses the main frame as a location point
   * 
   * @param frame
   */
  public CheckinDocDialog(JFrame frame) {
    super(frame, "Check-in document", true);

    // Get the parent container
    final Container cont = getContentPane();
    inputPanel = new CheckinInputPanel();

    cont.add(inputPanel, BorderLayout.CENTER);

    // Customize button
    getOkButton().setText("Check in");

    // Show it in the center of the frame
    setLocationRelativeTo(frame);
    setResizable(true);
    setModal(true);
    pack();
    setVisible(true);
  }

  // TODO: I doubt this decision
  public String getCommitMessage() {
    return inputPanel.getCommitMessage();
  }

  // TODO: I doubt this decision
  public String getVersioningState() {
    return inputPanel.getVersioningState();
  }
}

/**
 * Panel that constructs the main components and gets the input data
 * 
 * @author bluecc
 *
 */
class CheckinInputPanel extends JPanel implements ActionListener {
  private JLabel messageLabel, versionLabel;
  private JTextArea commitArea;
  private JRadioButton radioItemMajor;
  private JRadioButton radioItemMinor;

  private String versioningState;

  /**
   * Constructor of the main panel
   */
  public CheckinInputPanel() {
    // Set the layout
    setLayout(new GridBagLayout());

    // --Message
    messageLabel = new JLabel("Commit message:");
    // messageLabel.setOpaque(true);
    // messageLabel.setBackground(Color.BLUE);
    final GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    // c.weightx = 0;
    // c.ipadx = 10;
    c.gridwidth = 1;
    c.insets = new Insets(1, 5, 3, 5);
    c.fill = GridBagConstants.BOTH;
    add(messageLabel, c);

    // --Commit area
    commitArea = new JTextArea("Enter your message here", 5, 4);
    commitArea.selectAll();
    // commitArea.setOpaque(true);
    // commitArea.setBackground(Color.RED);
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 3;
    c.weightx = 1;
    // c.ipadx = 10;
    // c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstants.BOTH;
    add(commitArea, c);

    // --Version
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

    // --MAJOR
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
    c.anchor = GridBagConstants.WEST;
    c.weightx = 0.5;
    // c.ipadx = 10;
    // c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstants.BOTH;
    add(radioItemMajor, c);

    // --MINOR
    radioItemMinor = new JRadioButton("Minor");
    // radioItemMinor.setOpaque(true);
    // radioItemMinor.setBackground(Color.yellow);
    radioItemMinor.setActionCommand("MINOR");
    radioItemMinor.addActionListener(this);
    c.anchor = GridBagConstants.WEST;
    c.gridx = 2;
    c.gridy = 2;
    c.gridwidth = 1;
    c.weightx = 0.5;
    // c.ipadx = 10;
    // c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstants.BOTH;
    add(radioItemMinor, c);

    // This solves the problem where the dialog was not getting
    // focus the second time it was displayed
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        commitArea.requestFocusInWindow();
      }
    });

  }

  @Override
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equals("MAJOR")) {
      radioItemMinor.setSelected(false);
      versioningState = e.getActionCommand();
    }

    if (e.getActionCommand().equals("MINOR")) {
      radioItemMajor.setSelected(false);
      versioningState = e.getActionCommand();
    }
  }

  public String getVersioningState() {
    return this.versioningState;
  }

  public String getCommitMessage() {
    return this.commitArea.getText();
  }
}
