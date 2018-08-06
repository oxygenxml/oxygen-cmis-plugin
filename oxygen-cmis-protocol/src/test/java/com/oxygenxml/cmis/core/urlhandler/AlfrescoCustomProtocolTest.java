package com.oxygenxml.cmis.core.urlhandler;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.junit.Before;
import org.junit.Test;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ConnectionTestBase;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.SearchController;
import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

import ro.sync.net.protocol.FolderEntryDescriptor;

public class AlfrescoCustomProtocolTest extends ConnectionTestBase {

	private ResourceController ctrl;
	Logger logger = Logger.getLogger(AlfrescoCustomProtocolTest.class.getName());
	private final static String serverUrl = "http://127.0.0.1:8098/alfresco/api/-default-/public/cmis/versions/1.1/atom";

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

		List<Repository> repositoryList = CMISAccess.getInstance().connectToServerGetRepositories(url, null);

		Repository repository = repositoryList.get(0);

		CMISAccess.getInstance().connectToRepo(url, repository.getId());

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

	/**
	 * Get cmis object from Alfresco using URL
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetObject() throws IOException {
		String url = "https%3A%2F%2Fhttp%253A%252F%252F127.0.0.1" + "%253A8098%252Falfresco%252Fapi%252F-default-"
				+ "%252Fpublic%252Fcmis%252Fversions%252F1.1%252Fatom%2F-default-"
				+ "%2Fsamples%2Fflowers%2Fflowers.ditamap";

		url = URLDecoder.decode(url, "UTF-8");

		Document doc = (Document) getObjectFromURL(url, serverUrl);

		System.out.println("URL: " + url);
		System.out.println("DOC_NAME: " + doc.getName());

	}

	@Test
	public void testListFolder() throws MalformedURLException, IOException {
		String url = "https%3A%2F%2Fhttp%253A%252F%252F127.0.0.1" + "%253A8098%252Falfresco%252Fapi%252F-default-"
				+ "%252Fpublic%252Fcmis%252Fversions%252F1.1%252Fatom%2F-default-"
				+ "%2Fsamples%2Fflowers%2Fflowers.ditamap";

		url = URLDecoder.decode(url, "UTF-8");

		// url = url.replaceAll("cmis://", "");

		CmisURLConnection cuc = new CmisURLConnection(new URL(url), CMISAccess.getInstance());
		FileableCmisObject object = (FileableCmisObject) cuc.getCMISObject(url);

		System.out.println(object.getName());

		List<Folder> fldPath = object.getParents();

		int i = 0;
		for (Folder fld : fldPath) {
			System.out.println(++i + " " + fld.getName());
		}

		Folder parent = fldPath.get(fldPath.size() - 1);

		System.out.println(parent.getName());

		List<FolderEntryDescriptor> list = new ArrayList<FolderEntryDescriptor>();
		// dump(folderImpl, list, ctrl);

		// FolderImpl folderImpl = new FolderImpl(cuc.getRootFolder(new URL(url)));

		for (CmisObject obj : parent.getChildren()) {
			list.add(new FolderEntryDescriptor(CmisURLConnection.generateURLObject(obj, ctrl)));
			// IResource childResource = (IResource) iterator.next();
		}

		for (FolderEntryDescriptor fed : list) {
			System.out.println(fed.getAbsolutePath());
		}

	}

	/*
	 * private void dump(IResource resource, List<FolderEntryDescriptor> list,
	 * ResourceController ctrl) throws UnsupportedEncodingException {
	 * 
	 * 
	 * CmisObject obj = ctrl.getCmisObj(resource.getId()); if(obj instanceof Folder)
	 * { list.add(new FolderEntryDescriptor(CmisURLConnection.generateURLObject(obj,
	 * ctrl))); }
	 * 
	 * Iterator<IResource> iterator = resource.iterator(); while
	 * (iterator.hasNext()) { IResource childResource = (IResource) iterator.next();
	 * dump(childResource, list, ctrl); } }
	 */

}