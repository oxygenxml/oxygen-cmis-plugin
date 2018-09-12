package com.oxygenxml.cmis.web;

import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

public class TESTforTESTING {
	/**
	 * Executes operations over the resources.
	 */
	private CMISAccess cmisAccess;
	private URL serverUrl;
	private ResourceController ctrl;
	private CmisURLConnection connection;

	// cmis://http%3A%2F%2Fbasil%3A9080%2Ffncmis%2Fresources%2FService/TARGETOS/UsingAutoClassify/SubFolder/ibm.dita

	@Before
	public void setUp() throws Exception {
		serverUrl = new URL("http://basil:9080/fncmis/resources/Service");

		cmisAccess = new CMISAccess();
		

		UserCredentials uc = new UserCredentials("P8Admin", "Stil00", false);
		
		cmisAccess.connectToRepo(serverUrl, "TARGETOS", uc);
		ctrl = cmisAccess.createResourceController();

		connection = new CmisURLConnection(serverUrl, cmisAccess, new UserCredentials("P8Admin", "Stil00", false));
	}

	@Test
	public void testListOldVersions() throws Exception {

		String url = "cmis://http%3A%2F%2Fbasil%3A9080%2Ffncmis%2Fresources%2FService/TARGETOS/UsingAutoClassify/SubFolder/ibm.dita";

		Document document = (Document) connection.getCMISObject(url);

		String parPth = document.getParents().get(0).getPath();
		System.out.println(parPth);
		
		String url1 = CmisURLConnection.generateURLObject(document, ctrl, parPth);
		
		
		System.out.println(url1);
		/*
		 * for ( Document doc : document.getAllVersions()) {
		 * System.out.println(doc.getName()); }
		 * 
		 * String test = ListOldVersionsAction.listOldVersions(document, url);
		 * 
		 * System.out.println(test);
		 */
	}
}
