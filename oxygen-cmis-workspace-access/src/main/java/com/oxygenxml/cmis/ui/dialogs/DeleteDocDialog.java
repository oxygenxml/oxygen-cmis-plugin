package com.oxygenxml.cmis.ui.dialogs;

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

import org.apache.batik.ext.swing.GridBagConstants;

import com.oxygenxml.cmis.plugin.Tags;
import com.oxygenxml.cmis.plugin.TranslationResourceController;

import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;

/**
 * Dialog thats extends a dialog with cancel and ok button and uses a main panel
 * DeleteInputPanel
 * 
 * @author bluecc
 *
 */
public class DeleteDocDialog extends OKCancelDialog {
  
  private final DeleteInputPanel inputPanel;

  public DeleteDocDialog(JFrame frame) {
    super(frame, TranslationResourceController.getMessage(Tags.DELETE_DOCUMENT_DIALOG_TITLE), true);

    String cancelButtonValue = TranslationResourceController.getMessage(Tags.CANCEL_BUTTON_DELETE_DOCUMENT_DIALOG);
    String okButtonValue = TranslationResourceController.getMessage(Tags.OK_BUTTON_DELETE_DOCUMENT_DIALOG);

    // Get the parent container
    final Container cont = getContentPane();
    inputPanel = new DeleteInputPanel();

    cont.add(inputPanel, BorderLayout.CENTER);

    getOkButton().setText(okButtonValue);
    getCancelButton().setText(cancelButtonValue);

    // Show it in the center of the frame
    setLocationRelativeTo(frame);
    setResizable(true);
    setModal(true);
    pack();
    setVisible(true);
  }

  public String getDeleteType() {
    return inputPanel.getDeleteType();
  }
}

class DeleteInputPanel extends JPanel implements ActionListener {
  // Internal role
  private static final String SINGLE_VERSION = "SINGLE";
  private static final String ALL_VERSIONS = "ALL";
  private final JLabel messageLabel;
  private final JLabel versionLabel;
  private final JRadioButton radioItemAll;
  private final JRadioButton radioItemSingle;

  private String deleteType;

  public DeleteInputPanel() {
    setLayout(new GridBagLayout());

    // To be translated
    String allVersionsLabel = TranslationResourceController.getMessage(Tags.ALL_VERSIONS_LABEL);
    String messageValueLabel = TranslationResourceController.getMessage(Tags.MESSAGE_DELETE_DOCUMENT_DIALOG_LABEL);
    String singleVersionLabel = TranslationResourceController.getMessage(Tags.SINGLE_VERSION_LABEL);
    String versionLabelValue = TranslationResourceController.getMessage(Tags.VERSION_LABEL) + ":";
    
    // --MessageLabel
    messageLabel = new JLabel(messageValueLabel);
    final GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;

    c.gridwidth = 3;
    c.insets = new Insets(1, 5, 3, 5);
    c.fill = GridBagConstants.BOTH;
    add(messageLabel, c);

    // -- VersionLabel
    versionLabel = new JLabel(versionLabelValue);
    c.gridx = 0;
    c.gridy = 1;
    c.anchor = GridBagConstants.WEST;
    c.gridwidth = 1;
    c.weightx = 0;

    c.fill = GridBagConstants.VERTICAL;
    add(versionLabel, c);

    // --All versions
    radioItemAll = new JRadioButton(allVersionsLabel);

    radioItemAll.setActionCommand(ALL_VERSIONS);
    radioItemAll.addActionListener(this);
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstants.WEST;
    c.weightx = 0.5;
    c.fill = GridBagConstants.BOTH;
    add(radioItemAll, c);

    // --SINGLE
    radioItemSingle = new JRadioButton(singleVersionLabel);
    radioItemSingle.setActionCommand(SINGLE_VERSION);
    radioItemSingle.addActionListener(this);
    // Set selected
    radioItemSingle.setSelected(true);
    deleteType = SINGLE_VERSION;

    c.anchor = GridBagConstants.WEST;
    c.gridx = 2;
    c.gridy = 1;
    c.gridwidth = 1;
    c.weightx = 0.5;
    c.fill = GridBagConstants.BOTH;
    add(radioItemSingle, c);

  }

  @Override
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equals(ALL_VERSIONS)) {
      radioItemSingle.setSelected(false);
      deleteType = e.getActionCommand();
    }

    if (e.getActionCommand().equals(SINGLE_VERSION)) {
      radioItemAll.setSelected(false);
      deleteType = e.getActionCommand();
    }
  }

  public String getDeleteType() {
    return this.deleteType;
  }
}
