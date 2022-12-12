package com.oxygenxml.cmis.web;

import java.net.MalformedURLException;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;
import ro.sync.exml.plugin.workspace.security.Response;
import ro.sync.exml.plugin.workspace.security.TrustedHostsProviderExtension;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionChangedEvent;
import ro.sync.exml.workspace.api.options.WSOptionListener;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;

/**
 * {@link TrustedHostsProviderExtension} implementation that trust imposed host.
 */
@Slf4j
public class TrustedHostsProvider implements TrustedHostsProviderExtension {
 
  /**
   * Enforced host.
   */
  private String enforcedHost = null;

  /**
   * Constructor.
   */
  public TrustedHostsProvider() {
    WSOptionsStorage optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
    updateEnforcedHost(optionsStorage);

    optionsStorage.addOptionListener(new WSOptionListener(CmisPluginConfigExtension.ENFORCED_URL) {
      @Override
      public void optionValueChanged(WSOptionChangedEvent event) {
        updateEnforcedHost(optionsStorage);
      }
    });
  }

  /**
   * Update the enforced host field.
   */
  private void updateEnforcedHost(WSOptionsStorage optionsStorage) {
    this.enforcedHost = null;

    String enforcedUrl = optionsStorage.getOption(CmisPluginConfigExtension.ENFORCED_URL, "");
    if (enforcedUrl != null && !enforcedUrl.isEmpty()) {
      try {
        URL url = new URL(enforcedUrl);
        this.enforcedHost = url.getHost() + ":" + (url.getPort() != -1 ? url.getPort() : url.getDefaultPort());
      } catch (MalformedURLException e) {
        log.warn(e.getMessage(), e);
      }
    }
  }

  @Override
  public Response isTrusted(String hostName) {
    if (hostName.equals(this.enforcedHost)) {
      return TrustedHostsProvider.TRUSTED;
    } else {
      return TrustedHostsProvider.UNKNOWN;
    }
  }
}
