package com.oxygenxml.cmis.web;

import java.util.HashMap;

import javax.servlet.ServletException;

import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.PluginConfigExtension;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CmisPluginConfigExtension extends PluginConfigExtension {

	public static final String CHECKOUT_REQUIRED = "cmis.checkout_required";
	private static final String ENFORCED_URL = "cmis.enforced_url";
	private static final String ENFORCED_NAME = "cmis.enforced_name";
	private static final String ENFORCED_ICON = "cmis.enforced_icon";

	@Override
	public void init() throws ServletException {
		super.init();
		HashMap<String, String> defaultOptions = new HashMap<String, String>();
		defaultOptions.put(CHECKOUT_REQUIRED, "off");
		defaultOptions.put(ENFORCED_URL, "");
		defaultOptions.put(ENFORCED_NAME, "Local");
		defaultOptions.put(ENFORCED_ICON, "");
		setDefaultOptions(defaultOptions);
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
		
		StringBuilder optionsForm = new StringBuilder();
		PluginResourceBundle rb = ((WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace())
				.getResourceBundle();

		optionsForm.append(
				"<div style='font-family:robotolight, Arial, Helvetica, sans-serif;font-size:0.85em;font-weight: lighter'>")
				.append("<form style='text-align:left;line-height: 1.7em;'>");
		// Enforced URL
		optionsForm.append("<label style='margin-top:6px;display:block;font-size:120%;'>")
				.append(rb.getMessage(TranslationTags.ENFORCED_SERVER)).append(": ").append("<input placeholder='")
				.append(rb.getMessage(TranslationTags.SERVER_URL)).append("' name='").append(ENFORCED_URL)
				.append("' type='text' style='color:#606060;background-color:#FAFAFA;")
				.append("-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;display: block;")
				.append("width:100%;border-radius:4px;border:1px solid #E4E4E4;padding:6px 4px' value='")
				.append(enforcedUrl).append("'/>").append("</label>");
		// Enforced server note
		optionsForm.append(
				"<div style='background-color: lightyellow;border: 1px solid #dadab4; padding: 8px;margin-top: 5px;'>")
				.append(rb.getMessage(TranslationTags.ENFORCED_SERVER_NOTE)).append("</div>");
		// Check-out required.
		optionsForm.append("<br><label style='margin-bottom:6px;overflow:hidden;font-size:120%;'>").append("<input name='")
				.append(CHECKOUT_REQUIRED).append("' type=\"checkbox\" value=\"off\"")
				.append((isLockEnabled ? "checked" : "")).append("> ")
				.append(rb.getMessage(TranslationTags.CHECKOUT_REQUIRED_RESOURCE)).append("</label>");
		//Server name
		optionsForm.append("<label style='margin-top:6px;display:block;font-size:120%'>")
				.append(rb.getMessage(TranslationTags.SERVER_NAME)).append(": ").append("<input placeholder='")
				.append(rb.getMessage(TranslationTags.SERVER_NAME)).append("' name='").append(ENFORCED_NAME)
				.append("' type='text' style='color:#606060;background-color:#FAFAFA;")
				.append("-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;display: block;")
				.append("width:100%;border-radius:4px;border:1px solid #E4E4E4;padding:6px 4px' value='")
				.append(enforcedName).append("'/>").append("</label>");
		//Icon URL
		optionsForm.append("<label style='margin-top:6px;display:block;font-size:120%'>")
				.append(rb.getMessage(TranslationTags.ICON_URL)).append(": ").append("<input placeholder='")
				.append(rb.getMessage(TranslationTags.ICON_URL)).append("' name='").append(ENFORCED_ICON)
				.append("' type='text' style='color:#606060;background-color:#FAFAFA;")
				.append("-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;display: block;")
				.append("width:100%;border-radius:4px;border:1px solid #E4E4E4;padding:6px 4px' value='")
				.append(enforcedIcon).append("'/>").append("</label>");

		optionsForm.append("</form>").append("</div>");

		return optionsForm.toString();
	}

	@Override
	public String getOptionsJson() {
		return "{" 
				+ "\"" + ENFORCED_URL + "\":\"" + getOption(ENFORCED_URL, "") + "\"," 
				+ "\"" + ENFORCED_NAME + "\":\"" + getOption(ENFORCED_NAME, "") + "\","
				+ "\"" + ENFORCED_ICON + "\":\"" + getOption(ENFORCED_ICON, "") + "\""
				+ "}";
	}

}
