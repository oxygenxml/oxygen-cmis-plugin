package com.oxygenxml.cmis.core.urlhandler;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.junit.Before;
import org.junit.Test;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IDocument;

public class testAlfrescoCustomProtocol {
	private ResourceController ctrl;
	
	/**
	 * Conection to Alfresco Server
	 * @throws MalformedURLException
	 */
	@Before
	public void setUp() throws MalformedURLException {
		URL url = new URL("http://127.0.0.1:8098/alfresco/api/-default-/public/cmis/versions/1.1/atom");

		List<Repository> serverReposList = 
				CMISAccess.getInstance().connectToServerGetRepositories(url, null);

		Repository repository = serverReposList.get(0);

		CMISAccess.getInstance()
				.connectToRepo(url, repository.getId());

		ctrl = CMISAccess.getInstance()
				.createResourceController();

	}

	/**
	 * Generate custom URL for Alfresco Server
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void testGenerateUrl() throws UnsupportedEncodingException {
		SearchController search = new SearchController(ctrl);
		ArrayList<IDocument> docs = search.queringDoc("flowers.ditamap");
		
		Document doc = docs.get(0).getDoc();
		assertNotNull(doc);

		System.out.println(doc.getName());
		
		String url = URLEncoder
				.encode(CmisURLExtension
						.getCustomURL(doc, ctrl), "UTF-8");
		
		System.out.println(CmisURLExtension.getCustomURL(doc, ctrl));
		
		System.out.println(url);
	}
	
	/**
	 * Get cmis object from Alfresco using URL
	 * @throws UnsupportedEncodingException
	 
	 * @throws MalformedURLException
	 */
	@Test
	public void testGetObject() throws UnsupportedEncodingException, MalformedURLException {
		String encodedURL = 
				"cmis%3A%2F%2Fhttp%253A%252F%252F127.0.0.1%253A8098%252Falfresco"
				+ "%252Fapi%252F-default-%252Fpublic%252Fcmis%252Fversions%252F1.1%252Fatom"
				+ "%2F-default-%2Fsamples%2Fflowers%2Fflowers.ditamap";
		
		String url = URLDecoder.decode(encodedURL, "UTF-8");
		
		CmisURLExtension cpe = new CmisURLExtension();
		Document doc = (Document) new CmisURLConnection(new URL(url), CMISAccess.getInstance()).getCMISObject(url);
		
		System.out.println(url);
		System.out.println(doc.getName());
		
	}
}
