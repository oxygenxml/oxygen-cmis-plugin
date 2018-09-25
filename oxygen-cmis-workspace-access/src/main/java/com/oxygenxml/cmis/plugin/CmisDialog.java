package com.oxygenxml.cmis.plugin;

import java.awt.BorderLayout;
import java.awt.Container;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.ui.ControlComponents;

import ro.sync.ecss.extensions.commons.ui.OKCancelDialog;
import ro.sync.exml.workspace.api.standalone.InputURLChooser;

public class CmisDialog extends OKCancelDialog {
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(CmisDialog.class);
  private final ControlComponents inputPanel;
  private transient final InputURLChooser chooser;

  public CmisDialog(JFrame parentFrame, String title, InputURLChooser chooser, boolean modal) {
    super(parentFrame, title, modal);
    this.chooser = chooser;

    // Get the parent container
    final Container cont = getContentPane();

    inputPanel = new ControlComponents((Document doc) ->

    logger.debug("Open " + doc.getName()));

    cont.add(inputPanel, BorderLayout.CENTER);

    // Show it in the center of the frame
    setLocationRelativeTo(parentFrame);
    setResizable(true);
    setModal(true);
    pack();
    setVisible(true);

    // This solves the problem where the dialog was not getting
    // focus the second time it was displayed
    SwingUtilities.invokeLater(this::requestFocusInWindow);
  }

  @Override
  protected void doOK() {
    try {
      chooser.urlChosen(new URL(inputPanel.getSelectedURL()));
    } catch (MalformedURLException e) {

      logger.debug("Exception", e);
    }

    super.doOK();
  }
}
