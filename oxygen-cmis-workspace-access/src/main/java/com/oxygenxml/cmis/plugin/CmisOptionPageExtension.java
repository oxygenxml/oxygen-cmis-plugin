package com.oxygenxml.cmis.plugin;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ro.sync.exml.plugin.option.OptionPagePluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CmisOptionPageExtension extends OptionPagePluginExtension {

  private final String optionsTitle;
  private final String allowEditLabel;

  // Internal role
  private static final String ALLOW_EDIT_OPTION_TRUE = "true";
  private static final String ALLOW_EDIT_OPTION_FALSE = "false";
  public static final String ALLOW_EDIT = "ALLOW_EDIT";
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(CmisOptionPageExtension.class);
  private final JPanel mainPanel;

  private final JCheckBox allowEditCheckout;

  public CmisOptionPageExtension() {


    optionsTitle = TranslationResourceController.getMessage("OPTIONS_CMIS_TITLE");
    allowEditLabel = TranslationResourceController.getMessage("ALLOW_EDIT_WITHOUT_CHECKOUT");

    // Set logger level

    mainPanel = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    // Allow edit without checkout
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.ipadx = 10;
    c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstraints.HORIZONTAL;
    allowEditCheckout = new JCheckBox(allowEditLabel);

    mainPanel.add(allowEditCheckout, c);

  }

  @Override
  public void apply(PluginWorkspace pluginWorkspace) {

    try {
      if (allowEditCheckout.isSelected()) {
        pluginWorkspace.getOptionsStorage().setOption(ALLOW_EDIT, ALLOW_EDIT_OPTION_TRUE);
      } else {

        pluginWorkspace.getOptionsStorage().setOption(ALLOW_EDIT, ALLOW_EDIT_OPTION_FALSE);
      }

    } catch (Exception e) {
      logger.debug("Exception", e);
    }

  }

  @Override
  public void restoreDefaults() {
    allowEditCheckout.setSelected(false);
  }

  @Override
  public String getTitle() {

    return optionsTitle;
  }

  @Override
  public JComponent init(PluginWorkspace pluginWorkspace) {
    String allowEdit = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage().getOption(ALLOW_EDIT,
        ALLOW_EDIT_OPTION_FALSE);
    allowEditCheckout.setSelected(Boolean.valueOf(allowEdit));
    return mainPanel;
  }

}
