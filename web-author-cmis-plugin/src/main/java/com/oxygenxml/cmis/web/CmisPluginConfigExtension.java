package com.oxygenxml.cmis.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.servlet.ServletException;

import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.PluginConfigExtension;
import ro.sync.exml.plugin.workspace.security.TrustedHostsProvider;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class CmisPluginConfigExtension extends PluginConfigExtension {

	private static final String defaultAutoSaveInterval = "5";

	public static final String 	CHECKOUT_REQUIRED  = "cmis.checkout_required";
	public static final String ENFORCED_URL 	   = "cmis.enforced_url";
	private static final String ENFORCED_NAME      = "cmis.enforced_name";
	private static final String ENFORCED_ICON      = "cmis.enforced_icon";
	private static final String AUTOSAVE_INTERVAL  = "cmis.autosave_interval";

	@Override
	public void init() throws ServletException {
		super.init();
		String defaultName = "CMIS";
		
		HashMap<String, String> defaultOptions = new HashMap<>();
		defaultOptions.put(CHECKOUT_REQUIRED, "off");
		defaultOptions.put(ENFORCED_URL, "");
		defaultOptions.put(ENFORCED_NAME, defaultName);
		defaultOptions.put(ENFORCED_ICON, "");

		// Set default name if missing to show the tab on dashboard.
		if (getOption(ENFORCED_NAME, null) == null) {
		  setOption(ENFORCED_NAME, defaultName);
		}
		setDefaultOptions(defaultOptions);

    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
    if (pluginWorkspace instanceof StandalonePluginWorkspace) {
      ((StandalonePluginWorkspace) pluginWorkspace).addTrustedHostsProvider(
          new TrustedHostsProvider() {
            @Override
            public Response isTrusted(String hostName) {
              String trustedHost = null;

              String enforcedUrl = getOption(ENFORCED_URL, "");
              if (enforcedUrl != null && !enforcedUrl.isEmpty()) {
                try {
                  URL url = new URL(enforcedUrl);
                  trustedHost = url.getHost() + ":" + (url.getPort() != -1 ? url.getPort() : url.getDefaultPort());
                } catch (MalformedURLException e) {
                  // Consider it as unknown.
                }
              }

              if (hostName.equals(trustedHost)) {
                return TrustedHostsProvider.TRUSTED;
              } else {
                return TrustedHostsProvider.UNKNOWN;
              }
            }
          }
        );
    }
	}

	@Override
	public String getPath() {
		return "cmis-config";
	}

	@Override
	public String getOptionsForm() {
		String optionValue = getOption(CHECKOUT_REQUIRED, "");
		boolean isLockEnabled = "on".equals(optionValue);

		String enforcedUrl = getOption(ENFORCED_URL, "");
		String enforcedName = getOption(ENFORCED_NAME, "");
		String enforcedIcon = getOption(ENFORCED_ICON, "");
		String autosaveInterval = getOption(AUTOSAVE_INTERVAL, defaultAutoSaveInterval);

		StringBuilder optionsForm = new StringBuilder();
		PluginResourceBundle rb = ((WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace())
				.getResourceBundle();

		optionsForm.append(
				"<div style='font-family:robotolight, Arial, Helvetica, sans-serif;font-size:0.85em;font-weight: lighter'>")
				.append("<form style='text-align:left;line-height: 1.7em;'>");
		// Enforced URL
		optionsForm.append("<label style='margin-top:6px;display:block;font-size:120%;'>")
				.append(rb.getMessage(TranslationTags.SERVER_URL)).append(": ").append("<input name='").append(ENFORCED_URL)
				.append("' type='text' style='color:#606060;background-color:#FAFAFA;")
				.append("-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;display: block;")
				.append("width:100%;border-radius:4px;border:1px solid #E4E4E4;padding:6px 4px' value='")
				.append(enforcedUrl).append("'/>").append("</label>");
		// Enforced server note
		optionsForm.append(
				"<div style='background-color: lightyellow;border: 1px solid #dadab4; padding: 8px;margin-top: 5px;'>")
		      .append(rb.getMessage(TranslationTags.CMIS_SERVER_NOTE))
		      .append("<br>")
		      .append(rb.getMessage(TranslationTags.EXAMPLE) + ":")
		      .append("<br>")
		      .append("<span style=\"font-family: monospace;word-break: break-all;\">http://127.0.0.1:8098/alfresco/api/-default-/public/cmis/versions/1.1/atom</span>")
				.append("</div>");
		// Server name
		optionsForm.append("<label style='margin-top:6px;display:block;font-size:120%'>")
				.append(rb.getMessage(TranslationTags.SERVER_NAME)).append(": ")
				.append("<input name='").append(ENFORCED_NAME)
				.append("' type='text' style='color:#606060;background-color:#FAFAFA;")
				.append("-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;display: block;")
				.append("width:100%;border-radius:4px;border:1px solid #E4E4E4;padding:6px 4px' value='")
				.append(enforcedName).append("'/>").append("</label>");
		// Icon URL
		optionsForm.append("<label style='margin-top:6px;display:block;font-size:120%'>")
				.append(rb.getMessage(TranslationTags.SERVER_LOGO_URL)).append(": ").append("<input name='").append(ENFORCED_ICON)
				.append("' type='text' style='color:#606060;background-color:#FAFAFA;")
				.append("-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;display: block;")
				.append("width:100%;border-radius:4px;border:1px solid #E4E4E4;padding:6px 4px' value='")
				.append(enforcedIcon).append("'/>").append("</label>");
		// AutoSave interval
		optionsForm.append("<label style='margin-top:6px;display:block;font-size:120%'>")
				.append(rb.getMessage(TranslationTags.AUTOSAVE_INTERVAL)).append(" ")
				.append("(" + rb.getMessage(TranslationTags.SECONDS) + ")").append(": ")
				.append("<input name='").append(AUTOSAVE_INTERVAL)
				.append("' type='text' style='color:#606060;background-color:#FAFAFA;")
				.append("-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;display: block;")
				.append("width:100%;border-radius:4px;border:1px solid #E4E4E4;padding:6px 4px' value='")
				.append(autosaveInterval).append("'/>").append("</label>");
		// Check-out required.
		optionsForm.append("<br/><label style='margin-bottom:6px;margin-top:10px;overflow:hidden;font-size:120%;'>")
				.append("<input name='").append(CHECKOUT_REQUIRED).append("' type=\"checkbox\" value=\"off\"")
				.append((isLockEnabled ? "checked" : "")).append("> ")
				.append(rb.getMessage(TranslationTags.CHECK_OUT_REQUIRED)).append("</label>");

		optionsForm.append("</form>").append("</div>");

		return optionsForm.toString();
	}

	@Override
	public String getOptionsJson() {
		return "{" + "\"" + AUTOSAVE_INTERVAL + "\":\"" + getOption(AUTOSAVE_INTERVAL, defaultAutoSaveInterval) + "\"," 
				   + "\"" + ENFORCED_URL      + "\":\"" + getOption(ENFORCED_URL, "")   + "\"," 
				   + "\"" + ENFORCED_NAME     + "\":\"" + getOption(ENFORCED_NAME, "")  + "\"," 
				   + "\"" + ENFORCED_ICON     + "\":\"" + getOption(ENFORCED_ICON, "")  + "\"" + "}";
	}

}
