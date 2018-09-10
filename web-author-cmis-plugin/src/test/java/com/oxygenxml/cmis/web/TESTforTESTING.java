package com.oxygenxml.cmis.web;

import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
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
		cmisAccess.connectToRepo(serverUrl, "TARGETOS", new UserCredentials("P8Admin", "Stil00"));
		ctrl = cmisAccess.createResourceController();

		connection = new CmisURLConnection(serverUrl, cmisAccess, new UserCredentials("P8Admin", "Stil00"));
	}

	@Test
	public void testListOldVersions() throws Exception {

		String url = "cmis://http%3A%2F%2Fbasil%3A9080%2Ffncmis%2Fresources%2FService/TARGETOS/UsingAutoClassify/SubFolder/ibm.dita";

		Document document = (Document) connection.getCMISObject(url);

		String parPth = document.getParents().get(0).getPath();
		System.out.println(parPth);
		
		String url1 = CmisURLConnection.generateURLObject(document, ctrl, parPth);
		
		Document doooooc = ctrl.createVersionedDocument(document.getParents().get(0), "doc2.xml", "empty", "plain/xml", "VersionableType", VersioningState.MAJOR);
		String urldoooc = CmisURLConnection.generateURLObject(doooooc, ctrl, parPth);
		
		ObjectId id = doooooc.checkOut();
		Document pwc = (Document) ctrl.getSession().getObject(id);
		pwc.checkIn(true, null, null, "cooooment");
		
		doooooc = doooooc.getObjectOfLatestVersion(false);
		System.out.println(doooooc.getCheckinComment());
		
		
		
		System.out.println(doooooc.isPrivateWorkingCopy());
		
		System.out.println(url1);
		System.out.println(urldoooc);
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
