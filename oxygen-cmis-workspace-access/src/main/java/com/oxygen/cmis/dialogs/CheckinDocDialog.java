package com.oxygen.cmis.dialogs;

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

import com.oxygenxml.cmis.plugin.TranslationResourceController;

import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;

/**
 * Dialog that extends a dialog with cancel and ok buttons and uses a main panel
 * CheckinInputPanel.
 * 
 * @author bluecc
 *
 */
public class CheckinDocDialog extends OKCancelDialog {

  // Main inputPanel
  private final CheckinInputPanel inputPanel;

  /**
   * The constructor that uses the main frame as a location point
   * 
   * @param frame
   */
  public CheckinDocDialog(JFrame frame, String docName) {

    super(frame, TranslationResourceController.getMessage("CHECK_IN") +" "+ docName, true);

    String okButtonCheckinDialog = TranslationResourceController.getMessage("CHECK_IN");

    // Get the parent container
    final Container cont = getContentPane();
    inputPanel = new CheckinInputPanel();

    cont.add(inputPanel, BorderLayout.CENTER);

    // Customize button
    getOkButton().setText(okButtonCheckinDialog);

    // Show it in the center of the frame
    setLocationRelativeTo(frame);
    setResizable(true);
    setModal(true);
    pack();
    setVisible(true);
  }

  public String getCommitMessage() {
    return inputPanel.getCommitMessage();
  }

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

  // Internal role
  private static final String MAJOR_VERSION = "MAJOR";
  private static final String MINOR_VERSION = "MINOR";
  private static final String NONE_VERSION = "NONE";

  private final JLabel messageLabel;
  private final JLabel versionLabel;
  private final JTextArea commitArea;
  private final JRadioButton radioItemMajor;
  private final JRadioButton radioItemMinor;

  private String versioningState;

  /**
   * Constructor of the main panel
   */
  public CheckinInputPanel() {
    // To be translated
    String versioningStateMinorLabel = TranslationResourceController.getMessage("VERSIONING_STATE_MINOR_LABEL");
    String versioningStateMajorLabel = TranslationResourceController.getMessage("VERSIONING_STATE_MAJOR_LABEL");
    String versionLabelValue = TranslationResourceController.getMessage("VERSION_LABEL");
    String commitAreaLabelValue = TranslationResourceController
        .getMessage("COMMIT_AREA_CHECKIN_DIALOG_LABEL");
    String commitMessageValueLabel = TranslationResourceController
        .getMessage("COMMIT_MESSAGE_CHECKIN_DIALOG_LABEL");
    // Set the layout
    setLayout(new GridBagLayout());

    // --Message
    messageLabel = new JLabel(commitMessageValueLabel);

    final GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.insets = new Insets(1, 5, 3, 5);
    c.fill = GridBagConstants.BOTH;
    add(messageLabel, c);

    // Commit area
    commitArea = new JTextArea(commitAreaLabelValue, 5, 4);
    commitArea.selectAll();
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 3;
    c.weightx = 1;
    c.fill = GridBagConstants.BOTH;
    add(commitArea, c);

    // Version
    versionLabel = new JLabel(versionLabelValue);
    c.gridx = 0;
    c.gridy = 2;
    c.anchor = GridBagConstants.WEST;
    c.gridwidth = 1;
    c.weightx = 0;
    c.fill = GridBagConstants.VERTICAL;
    add(versionLabel, c);

    // MAJOR
    radioItemMajor = new JRadioButton(versioningStateMajorLabel);
    radioItemMajor.setActionCommand(MAJOR_VERSION);
    radioItemMajor.addActionListener(this);
    // Set selected
    radioItemMajor.setSelected(true);
    versioningState = MAJOR_VERSION;
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 1;
    c.anchor = GridBagConstants.WEST;
    c.weightx = 0.5;
    c.fill = GridBagConstants.BOTH;
    add(radioItemMajor, c);

    // MINOR
    radioItemMinor = new JRadioButton(versioningStateMinorLabel);
    radioItemMinor.setActionCommand(MINOR_VERSION);
    radioItemMinor.addActionListener(this);
    c.anchor = GridBagConstants.WEST;
    c.gridx = 2;
    c.gridy = 2;
    c.gridwidth = 1;
    c.weightx = 0.5;
    c.fill = GridBagConstants.BOTH;
    add(radioItemMinor, c);

    // This solves the problem where the dialog was not getting
    // focus the second time it was displayed
    SwingUtilities.invokeLater(commitArea::requestFocusInWindow);

  }

  @Override
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equals(MAJOR_VERSION)) {
      radioItemMinor.setSelected(false);
      versioningState = e.getActionCommand();
    }

    if (e.getActionCommand().equals(MINOR_VERSION)) {
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
