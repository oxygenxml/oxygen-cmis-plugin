package com.oxygenxml.cmis.plugin;

import javax.swing.JFrame;

import com.oxygenxml.cmis.ui.ControlComponents;
import com.oxygenxml.cmis.ui.constants.ImageConstants;

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * Plugin extension - workspace access extension.
 */
public class CMISWorkspaceAccessPluginExtension implements WorkspaceAccessPluginExtension {
  /**
   * ID of the specialized CMIS explorer view.
   */
  private static final String COM_OXYGENXML_CMIS_PLUGIN_CMIS_PLUGIN_VIEW = "com.oxygenxml.cmis.plugin.CMISPlugin.View";

  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
   */
  @Override
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {

    pluginWorkspaceAccess.addViewComponentCustomizer(viewInfo -> {
      if (COM_OXYGENXML_CMIS_PLUGIN_CMIS_PLUGIN_VIEW.equals(viewInfo.getViewID())) {

        viewInfo.setComponent(new ControlComponents());

        viewInfo.setIcon(ImageConstants.getImage(ImageConstants.CMIS_ICON));

        // Set name for the plugin
        String cmisExplorerName = TranslationResourceController.getMessage(Tags.CMIS_EXPLORER_NAME);
        viewInfo.setTitle(cmisExplorerName);
      }
    });
    
    JFrame mainFrame = (JFrame) pluginWorkspaceAccess.getParentFrame();
    
    pluginWorkspaceAccess.addInputURLChooserCustomizer(new BrowseCMISCustomizer(mainFrame));
  }

  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationClosing()
   */
  @Override
  public boolean applicationClosing() {
    // You can reject the application closing here

    return true;
  }
}