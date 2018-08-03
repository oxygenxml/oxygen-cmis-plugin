package com.oxygenxml.cmis.core.urlhandler;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.junit.Before;
import org.junit.Test;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IDocument;

public class AlfrescoCustomProtocolTest {
	private ResourceController ctrl;
	Logger logger = Logger.getLogger(AlfrescoCustomProtocolTest.class.getName());

	//private String serverUrl = "http://127.0.0.1:8098/alfresco/api/-default-/public/cmis/versions/1.1/atom";

	/**
	 * Conection to Alfresco Server
	 * 
	 * @throws MalformedURLException
	 */
	@Before
	public void setUp() throws MalformedURLException {
		URL url = new URL("http://127.0.0.1:8098/alfresco/api/-default-/public/cmis/versions/1.1/atom");

		List<Repository> repositoryList = CMISAccess.getInstance().getRepositories(url, null);

		Repository repository = repositoryList.get(0);

		CMISAccess.getInstance().connect(url, repository.getId());

		ctrl = CMISAccess.getInstance().createResourceController();

	}

	/**
	 * Generate custom URL for Alfresco Server
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void testGenerateUrl() throws UnsupportedEncodingException {
		SearchController search = new SearchController(ctrl);
		ArrayList<IDocument> docs = search.queringDoc("flowers.ditamap");

		Document doc = docs.get(0).getDoc();
		assertNotNull(doc);

		String url = CmisURLExtension.getCustomURL(doc, ctrl);

		System.out.println("gURL: " + URLEncoder.encode(url, "UTF-8"));
	}

	@Test
	public void testGetRootUrl() throws UnsupportedEncodingException {
		Folder root = ctrl.getRootFolder();

		String url = CmisURLExtension.getCustomURL(root, ctrl);

		System.out.println(url);
	}

/*	*//**
	 * Get cmis object from Alfresco using URL
	 * 
	 * @throws IOException
	 *//*
	@Test
	public void testGetObject() throws IOException {
		String url = " cmis://http%3A%2F%2F127.0.0.1%3A8098%2Falfresco"
				+ "%2Fapi%2F-default-%2Fpublic%2Fcmis%2Fversions%2F1.1"
				+ "%2Fatom/-default-/samples/flowers/flowers.ditamap";

		// String url = URLDecoder.decode(encodedURL, "UTF-8");

		CmisURLExtension cpe = new CmisURLExtension();
		Document doc = (Document) cpe.getObjectFromURL(url, serverUrl);

		System.out.println("URL: " + url);
		System.out.println("DOC_NAME: " + doc.getName());

	}
*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}