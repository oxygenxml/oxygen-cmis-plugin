package com.oxygenxml.cmis.web;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.junit.Before;
import org.junit.Test;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import ro.sync.net.protocol.FolderEntryDescriptor;

public class AlfrescoCustomProtocolTest {

	private ResourceController ctrl;
	Logger logger = Logger.getLogger(AlfrescoCustomProtocolTest.class.getName());
	
	private final static String serverUrl = "http://127.0.0.1:8098/alfresco/api/-default-/public/cmis/versions/1.1/atom";
	private CMISAccess access;

	// private String serverUrl =
	// "http://127.0.0.1:8098/alfresco/api/-default-/public/cmis/versions/1.1/atom";

	/**
	 * Conection to Alfresco Server
	 * 
	 * @throws MalformedURLException
	 */
	@Before
	public void setUp() throws MalformedURLException {
		URL url = new URL("http://127.0.0.1:8098/alfresco/api/-default-/public/cmis/versions/1.1/atom");

		List<Repository> repositoryList = CMISAccess.getInstance().connectToServerGetRepositories(url,
				new UserCredentials("admin", "admin"));

		Repository repository = repositoryList.get(0);

		access = CMISAccess.getInstance();

		access.connectToRepo(url, repository.getId(), new UserCredentials("admin", "admin"));

		ctrl = CMISAccess.getInstance().createResourceController();

	}

	/**
	 * Generate custom URL for Alfresco Server
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGenerateUrl() throws IOException {
		SearchController search = new SearchController(ctrl);
		List<IResource> docs = search.queryDoc("flowers.dita");

		Document doc = ((IDocument) docs.get(0)).getDoc();
		assertNotNull(doc);
		

		String url = CmisURLConnection.generateURLObject(doc, ctrl);
		System.out.println("URL: " + URLEncoder.encode(url, "UTF-8"));
		System.out.println(url);

		System.out.println("TUT " + CMISAccess.getInstance().getSession().toString());

		Document doc1 = (Document) getObjectFromURL(url, serverUrl, new UserCredentials("admin", "admin"));
		System.out.println(doc1.getName());
	}

	@Test
	public void testGetRootUrl() throws UnsupportedEncodingException {
		Folder root = ctrl.getRootFolder();

		System.out.println(root.getName());

		String url = CmisURLConnection.generateURLObject(root, ctrl);

		System.out.println(url);
	}

	@Test
	public void testGetObject() throws IOException {
		String url = "https%3A%2F%2Fhttp%253A%252F%252F127.0.0.1" + "%253A8098%252Falfresco%252Fapi%252F-default-"
				+ "%252Fpublic%252Fcmis%252Fversions%252F1.1%252Fatom%2F-default-"
				+ "%2Fsamples_root%2Fflowers%2Fflowers.ditamap";

		url = URLDecoder.decode(url, "UTF-8");

		Document doc = (Document) getObjectFromURL(url, serverUrl, new UserCredentials("admin", "admin"));

		System.out.println("URL: " + url);
		System.out.println("DOC_NAME: " + doc.getName());

	}

	@Test
	public void testListFolder() throws Exception {
		String url = "https%3A%2F%2Fhttp%253A%252F%252F127.0.0.1" + "%253A8098%252Falfresco%252Fapi%252F-default-"
				+ "%252Fpublic%252Fcmis%252Fversions%252F1.1%252Fatom%2F-default-"
				+ "%2Fsamples_root%2Fflowers%2Fflowers.ditamap";

		url = URLDecoder.decode(url, "UTF-8");
		List<FolderEntryDescriptor> list = new ArrayList<FolderEntryDescriptor>();

		CmisURLConnection cuc = new CmisURLConnection(new URL(url), CMISAccess.getInstance(),
				new UserCredentials("admin", "admin"));
		FileableCmisObject object = (FileableCmisObject) cuc.getCMISObject(url);

		if (ctrl == null) {
			logger.info("ResourceController is null!");
		}

		logger.info("OBJ NAME ---> " + object.getName());

		List<Folder> fldPath = object.getParents();
		Folder parent = fldPath.get(0);

		System.out.println(parent.getName());

		logger.info("PARENT NAME ---> " + parent.getName());

		for (CmisObject obj : parent.getChildren()) {
			String entryUrl = CmisURLConnection.generateURLObject(obj, ctrl);
			entryUrl = entryUrl.concat((obj instanceof Folder) ? "/" : "");
			list.add(new FolderEntryDescriptor(entryUrl));
		}

		for (FolderEntryDescriptor fed : list) {
			System.out.println(fed.getAbsolutePath());
		}

	}

	@Test
	public void testReposList() throws IOException {
		CmisURLConnection cuc = new CmisURLConnection(new URL(serverUrl), CMISAccess.getInstance(),
				new UserCredentials("admin", "admin"));
		String sURL = "https://http%3A%2F%2F127.0.0.1%3A8098%2Falfresco%2Fapi%2F-default-%2Fpublic%2Fcmis%2Fversions%2F1.1%2Fatom/";

		List<Repository> reposList = cuc.getCMISAccess().connectToServerGetRepositories(
				CmisURLConnection.getServerURL(sURL, null), new UserCredentials("admin", "admin"));

		for (Repository repos : reposList) {
			System.out.println(repos.getName() + " " + repos.getId());
		}

		String url = generateRepoUrl(reposList.get(0), ctrl);

		System.out.println(url);
	}

	public String generateRepoUrl(Repository repo, ResourceController _ctrl)
			throws UnsupportedEncodingException, MalformedURLException {
		StringBuilder urlb = new StringBuilder();

		String originalProtocol = _ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);

		originalProtocol = URLEncoder.encode(originalProtocol, "UTF-8");

		urlb.append(("cmis" + "://")).append(originalProtocol).append("/");
		urlb.append(CMISAccess.getInstance().getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID));
		urlb.append("/");

		return urlb.toString();
	}

	public CmisObject getObjectFromURL(String url, String serverUrl, UserCredentials credentials) throws IOException {
		if (url == null) {
			throw new NullPointerException();
		}
		CmisURLConnection cuc = new CmisURLConnection(new URL(serverUrl), CMISAccess.getInstance(),
				new UserCredentials("admin", "admin"));

		return cuc.getCMISObject(url);
	}

}