package com.oxygenxml.cmis.plugin;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.oxygenxml.cmis.ui.ControlComponents;

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

        // Accepts only PNGs
        // You can have images located inside the JAR library and use them...
        // getClassLoader because no '/' is present in the front of the path
        URL resource = getClass().getClassLoader().getResource("images/cmis.png");

        viewInfo.setIcon(new ImageIcon(resource));

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