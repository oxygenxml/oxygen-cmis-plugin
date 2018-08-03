package com.oxygenxml.cmis.core.urlhandler;

import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

import ro.sync.ecss.extensions.api.webapp.plugin.FilterURLConnection;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;
import ro.sync.net.protocol.FolderEntryDescriptor;

public class CmisBrowsingURLConnection extends FilterURLConnection {

	public CmisBrowsingURLConnection(URLConnection delegateConnection) {
		super(delegateConnection);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<FolderEntryDescriptor> listFolder() throws IOException, UserActionRequiredException {
		// TODO: file browsing

	 //CmisURLConnection cuc = (CmisURLConnection) delegateConnection;
	 
	 new FolderEntryDescriptor(this.url.getPath() + "/entry.xml");
	 new FolderEntryDescriptor(this.url + "/folder" + "/");
	// cuc.getCMISObject(url);

	 return null;
		
	}	
}
