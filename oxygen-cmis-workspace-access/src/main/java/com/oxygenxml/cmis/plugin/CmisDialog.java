package com.oxygenxml.cmis.plugin;

import java.awt.BorderLayout;
import java.awt.Container;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.runtime.FolderImpl;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.CmisAccessSingleton;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.ui.ControlComponents;

import ro.sync.ecss.extensions.commons.ui.OKCancelDialog;
import ro.sync.exml.workspace.api.standalone.InputURLChooser;

/**
 * Browse through CMIS resources. 
 */
public class CmisDialog extends OKCancelDialog {
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(CmisDialog.class);
  private final ControlComponents inputPanel;
  private transient final InputURLChooser chooser;

  public CmisDialog(JFrame parentFrame, InputURLChooser chooser) {
    super(
        parentFrame, 
        TranslationResourceController.getMessage(Tags.CMIS_DIALOG), 
        true);
    
    this.chooser = chooser;

    // Get the parent container
    final Container cont = getContentPane();

    inputPanel = new ControlComponents();

    cont.add(inputPanel, BorderLayout.CENTER);

    // Show it in the center of the frame
    setLocationRelativeTo(parentFrame);
    setResizable(true);
    
    pack();
    setVisible(true);

    // This solves the problem where the dialog was not getting
    // focus the second time it was displayed
    SwingUtilities.invokeLater(this::requestFocusInWindow);
  }
  
  

  @Override
  protected void doOK() {
    try {
      CmisObject cmisObject = inputPanel.getSelectedCmisObject();
      
      if (cmisObject != null) {
        ResourceController resourceController = CmisAccessSingleton.getInstance().createResourceController();
        
        String url = CmisURLConnection.generateURLObject(cmisObject, resourceController);
        if (chooser.getBrowseMode() == InputURLChooser.SAVE_RESOURCE 
            &&  cmisObject instanceof FolderImpl) {
          // The user wants to save inside a folder. Append something.
          url = url + "/untitled.xml";
        }
        
        if (logger.isDebugEnabled()) {
          logger.debug("Save to " + url);
        }
        
        chooser.urlChosen(new URL(url));
      }
      
    } catch (MalformedURLException e) {
      logger.error(e, e);
    }

    super.doOK();
  }
}
