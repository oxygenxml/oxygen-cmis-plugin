package com.oxygenxml.cmis.web;

import java.util.HashMap;

import javax.servlet.ServletException;

import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.PluginConfigExtension;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CmisPluginConfigExtension extends PluginConfigExtension {

	private static final String ENFORCED_URL = "cmis.enforced_url";

	@Override
	public void init() throws ServletException {
		super.init();
		HashMap<String, String> defaultOptions = new HashMap<String, String>();
		defaultOptions.put(ENFORCED_URL, "");

		setDefaultOptions(defaultOptions);
	}

	@Override
	public String getPath() {
		return "cmis-config";
	}

	@Override
	public String getOptionsForm() {
		String enforcedUrl = getOption(ENFORCED_URL, "");
		StringBuilder optionsForm = new StringBuilder();
		PluginResourceBundle rb = ((WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace())
				.getResourceBundle();

		optionsForm.append(
				"<div style='font-family:robotolight, Arial, Helvetica, sans-serif;font-size:0.85em;font-weight: lighter'>")
				.append("<form style='text-align:left;line-height: 1.7em;'>");

		optionsForm.append("<label style='margin-top:6px;display:block;'>")
				.append(rb.getMessage(TranslationTags.ENFORCED_SERVER)).append(": ").append("<input placeholder='")
				.append(rb.getMessage(TranslationTags.SERVER_URL)).append("' name='").append(ENFORCED_URL)
				.append("' type='text' style='color:#606060;background-color:#FAFAFA;")
				.append("-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;display: inline-block;")
				.append("width:75%;border-radius:4px;border:1px solid #E4E4E4;padding:6px 4px' value='")
				.append(enforcedUrl).append("'/>").append("</label>");
		// Enforced server note
		optionsForm.append(
				"<div style='background-color: lightyellow;border: 1px solid #dadab4; padding: 8px;margin-top: 5px;'>")
				.append(rb.getMessage(TranslationTags.ENFORCED_SERVER_NOTE)).append("</div>");

		optionsForm.append("</form>").append("</div>");

		return optionsForm.toString();
	}

	@Override
	public String getOptionsJson() {
		return "{" + "\"" + ENFORCED_URL + "\":\"" + getOption(ENFORCED_URL, "") + "\"}";
	}

}
