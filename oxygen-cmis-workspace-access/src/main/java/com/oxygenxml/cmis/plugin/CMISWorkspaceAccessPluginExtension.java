package com.oxygenxml.cmis.plugin;

import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.chemistry.opencmis.client.api.Document;

import com.oxygenxml.cmis.ui.ControlComponents;
import com.oxygenxml.cmis.ui.TabsPresenter;

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer;
import ro.sync.exml.workspace.api.standalone.ViewInfo;

/**
 * Plugin extension - workspace access extension.
 */
public class CMISWorkspaceAccessPluginExtension implements WorkspaceAccessPluginExtension {

  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
   */
  @Override
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {

    pluginWorkspaceAccess.addViewComponentCustomizer(new ViewComponentCustomizer() {
      /**
       * @see ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer#customizeView(ro.sync.exml.workspace.api.standalone.ViewInfo)
       */
      @Override
      public void customizeView(ViewInfo viewInfo) {
        if (
        // The view ID defined in the "plugin.xml"
        "com.oxygenxml.cmis.plugin.CMISPlugin.View".equals(viewInfo.getViewID())) {

          viewInfo.setComponent(new ControlComponents(new TabsPresenter() {

            @Override
            public void presentItem(Document doc) {

              System.err.println("Open " + doc.getName());
            }
          }));
          viewInfo.setTitle("CMIS Explorer");
          
          //Accepts only PNGs
          // You can have images located inside the JAR library and use them...
          URL resource = getClass().getClassLoader().getResource("cmis.png");
    
          viewInfo.setIcon(new ImageIcon(resource));
  
        }
      }
    });
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