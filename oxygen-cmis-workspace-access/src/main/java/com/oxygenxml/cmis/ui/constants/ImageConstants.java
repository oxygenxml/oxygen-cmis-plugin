package com.oxygenxml.cmis.ui.constants;

import java.net.URL;

import javax.swing.ImageIcon;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.images.ImageUtilities;

/**
 * All image locations that are used in this plugin.
 */
public class ImageConstants {

  private ImageConstants() {
    // Nothing.
  }

  public static final String FOLDER_TREE_ICON = "/images/ChooseFolder16.png";
  public static final String LOCK_ICON = "/images/Lock16.png";
  public static final String SETTINGS_ICON = "/images/Settings16.png";
  public static final String FILE_ICON = "/images/UnknownFile16.png";
  public static final String CMIS_ICON = "/images/Cmis16.png";

  public static ImageIcon getImage(String image) {
    ImageIcon icon = null;
    ImageUtilities imageUtilities = PluginWorkspaceProvider.getPluginWorkspace().getImageUtilities();
    URL resource = ImageConstants.class.getResource(image);
    if (resource != null) {
      icon = (ImageIcon) imageUtilities.loadIcon(resource);
    }

    return icon;
  }
}
