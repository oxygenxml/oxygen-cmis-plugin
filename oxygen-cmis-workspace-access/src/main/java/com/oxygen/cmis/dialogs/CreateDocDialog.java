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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.batik.ext.swing.GridBagConstants;

import com.oxygenxml.cmis.plugin.TranslationResourceController;

import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;

public class CreateDocDialog extends OKCancelDialog {

  private final CreateDocInputPanel inputPanel;

  public CreateDocDialog(JFrame frame) {
    super(frame, TranslationResourceController.getMessage("CREATE_DOCUMENT_DIALOG_TITLE"), true);

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

  public String getFileName() {
    return inputPanel.getFileName();
  }

  public String getVersioningState() {
    return inputPanel.getVersioningState();
  }
}

class CreateDocInputPanel extends JPanel implements ActionListener {

  // Internal role
  private static final String NONE_STATE = "NONE";
  private static final String MINOR_STATE = "MINOR";
  private static final String MAJOR_STATE = "MAJOR";
  private final JLabel messageLabel;
  private final JLabel versionLabel;
  private final JTextField filename;
  private final JRadioButton radioItemMajor;
  private final JRadioButton radioItemMinor;
  private final JRadioButton radioItemNone;

  private String versioningState;

  public CreateDocInputPanel() {
    // To be translated
    String versioningStateNoneLabel = TranslationResourceController.getMessage("VERSIONING_STATE_NONE_LABEL");
    String versioningStateMinorLabel = TranslationResourceController.getMessage("VERSIONING_STATE_MINOR_LABEL");
    String versioningStateMajorLabel = TranslationResourceController.getMessage("VERSIONING_STATE_MAJOR_LABEL");
    String versionLabelValue = TranslationResourceController
        .getMessage("VERSION_CREATE_DOCUMENT_DIALOG_LABEL");
    String deafultFilenameValue = TranslationResourceController
        .getMessage("DEFAULT_FILENAME_CREATE_DOCUMENT_DIALOG");
    String messageLabelValue = TranslationResourceController
        .getMessage("MESSAGE_CREATE_DOCUMENT_DIALOG_LABEL");
    setLayout(new GridBagLayout());

    messageLabel = new JLabel(messageLabelValue);
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.insets = new Insets(0, 5, 3, 5);
    c.fill = GridBagConstants.BOTH;
    add(messageLabel, c);

    filename = new JTextField(deafultFilenameValue);
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 3;
    c.weightx = 1;

    c.fill = GridBagConstants.BOTH;
    add(filename, c);

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
    radioItemMajor.setActionCommand(MAJOR_STATE);
    radioItemMajor.addActionListener(this);
    // Set selected
    radioItemMajor.setSelected(true);
    versioningState = MAJOR_STATE;
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 1;
    c.anchor = GridBagConstants.CENTER;
    c.weightx = 0;
    c.fill = GridBagConstants.BOTH;
    add(radioItemMajor, c);

    // MINOR
    radioItemMinor = new JRadioButton(versioningStateMinorLabel);
    radioItemMinor.setActionCommand(MINOR_STATE);
    radioItemMinor.addActionListener(this);
    c.anchor = GridBagConstants.CENTER;
    c.gridx = 2;
    c.gridy = 2;
    c.gridwidth = 1;
    c.weightx = 0;
    c.fill = GridBagConstants.BOTH;
    add(radioItemMinor, c);

    // NONE
    radioItemNone = new JRadioButton(versioningStateNoneLabel);
    radioItemNone.setActionCommand(NONE_STATE);
    radioItemNone.addActionListener(this);
    c.anchor = GridBagConstants.CENTER;
    c.gridx = 3;
    c.gridy = 2;
    c.gridwidth = 1;
    c.weightx = 0;
    c.fill = GridBagConstants.BOTH;
    add(radioItemNone, c);

    // This solves the problem where the dialog was not getting
    // focus the second time it was displayed
    SwingUtilities.invokeLater(filename::requestFocusInWindow);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equals(MAJOR_STATE)) {
      radioItemMinor.setSelected(false);
      radioItemNone.setSelected(false);
      versioningState = e.getActionCommand();
    }

    if (e.getActionCommand().equals(MINOR_STATE)) {
      radioItemMajor.setSelected(false);
      radioItemNone.setSelected(false);
      versioningState = e.getActionCommand();
    }
    if (e.getActionCommand().equals(NONE_STATE)) {
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
