package com.oxygenxml.cmis.plugin;

import java.net.URL;

import javax.swing.JFrame;

import com.oxygenxml.cmis.ui.ControlComponents;
import com.oxygenxml.cmis.ui.constants.ImageConstants;

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.listeners.WSEditorListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * Plugin extension - workspace access extension.
 */
public class CMISWorkspaceAccessPluginExtension implements WorkspaceAccessPluginExtension {
  /**
   * ID of the specialized CMIS explorer view.
   */
  private static final String COM_OXYGENXML_CMIS_PLUGIN_CMIS_PLUGIN_VIEW = "com.oxygenxml.cmis.plugin.CMISPlugin.View";
  ControlComponents component;

  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
   */
  @Override
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {

    pluginWorkspaceAccess.addViewComponentCustomizer(viewInfo -> {
      if (COM_OXYGENXML_CMIS_PLUGIN_CMIS_PLUGIN_VIEW.equals(viewInfo.getViewID())) {
        component = new ControlComponents();
        viewInfo.setComponent(component);

        viewInfo.setIcon(ImageConstants.getImage(ImageConstants.CMIS_ICON));

        // Set name for the plugin
        String cmisExplorerName = TranslationResourceController.getMessage(Tags.CMIS_EXPLORER_NAME);
        viewInfo.setTitle(cmisExplorerName);
      }
    });

    JFrame mainFrame = (JFrame) pluginWorkspaceAccess.getParentFrame();

    pluginWorkspaceAccess.addInputURLChooserCustomizer(new BrowseCMISCustomizer(mainFrame));

    registerSaveListeners(pluginWorkspaceAccess, PluginWorkspace.MAIN_EDITING_AREA);
    registerSaveListeners(pluginWorkspaceAccess, PluginWorkspace.DITA_MAPS_EDITING_AREA);
    
  }

  /**
   * Registers listeners on the opened editors that will refresh the view when editors are saved (maybe with a different name).
   * 
   * @param pluginWorkspaceAccess Workspace access.
   * @param area One of {@link PluginWorkspace#MAIN_EDITING_AREA} or {@link PluginWorkspace#DITA_MAPS_EDITING_AREA}
   */
  private void registerSaveListeners(final StandalonePluginWorkspace pluginWorkspaceAccess, int area) {
    pluginWorkspaceAccess.addEditorChangeListener(new WSEditorChangeListener() {
      @Override
      public void editorOpened(URL editorLocation) {
        final WSEditor editorAccess = pluginWorkspaceAccess.getEditorAccess(editorLocation, area);
        editorAccess.addEditorListener(new WSEditorListener() {
          @Override
          public void editorSaved(int operationType) {
            URL savedURL = editorAccess.getEditorLocation();
            if (component != null) {
              component.refresh(savedURL);
            }
          }
        });
      }
    }, area);
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