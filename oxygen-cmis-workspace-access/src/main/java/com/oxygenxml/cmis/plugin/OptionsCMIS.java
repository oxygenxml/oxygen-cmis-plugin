package com.oxygenxml.cmis.plugin;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ro.sync.exml.plugin.option.OptionPagePluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;

public class OptionsCMIS extends OptionPagePluginExtension {
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(OptionsCMIS.class);
  private final JPanel mainPanel;

  private final JCheckBox allowEditCheckout;
  public static final String ALLOW_EDIT = "ALLOW_EDIT";

  public OptionsCMIS() {
    // Set logger level
    logger.setLevel(Level.DEBUG);

    mainPanel = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    // Allow edit without checkout
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.ipadx = 10;
    c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstraints.HORIZONTAL;
    allowEditCheckout = new JCheckBox("Allow edit without checkout (applies on Versionable documents)");
    allowEditCheckout.setSelected(false);
    mainPanel.add(allowEditCheckout, c);

  }

  @Override
  public void apply(PluginWorkspace pluginWorkspace) {

    try {
      if (allowEditCheckout.isSelected()) {
        pluginWorkspace.getOptionsStorage().setOption(ALLOW_EDIT, "true");
      } else {

        pluginWorkspace.getOptionsStorage().setOption(ALLOW_EDIT, "false");
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

    return "Options CMIS";
  }

  @Override
  public JComponent init(PluginWorkspace pluginWorkspace) {

    return mainPanel;
  }

}
