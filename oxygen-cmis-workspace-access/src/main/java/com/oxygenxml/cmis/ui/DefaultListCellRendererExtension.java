package com.oxygenxml.cmis.ui;

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Default renderer of the resources
 * 
 * @author bluecc
 *
 */
class DefaultListCellRendererExtension extends DefaultListCellRenderer {
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(DefaultListCellRendererExtension.class);

  @Override
  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {

    Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    if (component instanceof JLabel) {
      String renderText = "";

      if (value != null) {
        JLabel comLabel = ((JLabel) component);
        IResource resource = ((IResource) value);

        // Cast in order to use the methods from IResource interface
        renderText = resource.getDisplayName();

        // Set the label for the component
        comLabel.setText(renderText);

        renderItemsIcons(value, comLabel, resource);
      }
    }

    return component;
  }

  /**
   * Renders the icons specific to their type
   * 
   * @param value
   * @param comLabel
   * @param resource
   */
  private void renderItemsIcons(Object value, JLabel comLabel, IResource resource) {
    // If it's an instance of custom type of Folder.
    if ((IResource) value instanceof FolderImpl) {

      comLabel.setIcon(UIManager.getIcon("FileView.directoryIcon"));

    } else if (resource instanceof DocumentImpl) {
      DocumentImpl doc = ((DocumentImpl) resource);

      // Check if it's checked-out.
      if (doc.isCheckedOut()) {

        comLabel.setIcon(new ImageIcon(getClass().getResource("/images/padlock.png")));

      } else {
        // ---------Use Oxygen Icons.
        try {
          comLabel.setIcon((Icon) PluginWorkspaceProvider.getPluginWorkspace().getImageUtilities()
              .getIconDecoration(new URL("http://localhost/" + resource.getDisplayName())));

        } catch (final MalformedURLException e) {
          // If it's an instance of custom type of Folder.
          // Set the native icon to the component.
          comLabel.setIcon(UIManager.getIcon("FileView.fileIcon"));
          logger.error(e, e);

        }
        // ---------
      }
    }
  }
}