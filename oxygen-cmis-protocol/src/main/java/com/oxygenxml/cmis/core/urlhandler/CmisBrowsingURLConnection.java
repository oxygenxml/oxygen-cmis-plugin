package com.oxygenxml.cmis.core.urlhandler;

import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.ResourceController;
import ro.sync.ecss.extensions.api.webapp.plugin.FilterURLConnection;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;
import ro.sync.net.protocol.FolderEntryDescriptor;

public class CmisBrowsingURLConnection extends FilterURLConnection {
	private static final Logger logger = Logger.getLogger(CmisBrowsingURLConnection.class.getName());
	
	private CmisURLConnection cuc;
	private ResourceController ctrl;

	public CmisBrowsingURLConnection(URLConnection delegateConnection) {
		super(delegateConnection);
		this.cuc = (CmisURLConnection) delegateConnection;
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<FolderEntryDescriptor> listFolder() throws IOException, UserActionRequiredException {
		// TODO: file browsing

		List<FolderEntryDescriptor> list = new ArrayList<FolderEntryDescriptor>();
		// CmisURLConnection cuc = (CmisURLConnection) delegateConnection;
		
		logger.info("LIST FOLDER Method ---> " + url);
		
		//At first we need to get CmisObject 
		FileableCmisObject object = (FileableCmisObject) cuc.getCMISObject(url.toExternalForm());
		//After connection we get ResourceController for generate URL!
		ctrl = cuc.getCtrl(url);

		if(ctrl == null) {
			logger.info("ResourceController is null!");
		}
		
		
		logger.info("OBJ NAME ---> " + object.getName());
		
		List<Folder> fldPath = object.getParents();
		Folder parent = fldPath.get(fldPath.size() - 1);
		
		logger.info("PARENT NAME ---> " + parent.getName());
		
		for (CmisObject obj : parent.getChildren()) {
			list.add(new FolderEntryDescriptor(CmisURLConnection.generateURLObject(obj, ctrl)));
		}

		//LOGGING
		int i = 0;
		for(FolderEntryDescriptor fed : list) {
			logger.info(++i + ") " + fed.getAbsolutePath());
		}
		

		/*new FolderEntryDescriptor(this.url.getPath() + "/entry.xml");
		new FolderEntryDescriptor(this.url + "/folder" + "/");
		cuc.getCMISObject(url);*/

		return list;

	}

}
