package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;

public class SearchControllerTest extends ConnectionTestBase {

	private ResourceController ctrl;

	@Before
	public void setUp() throws MalformedURLException {
		CMISAccess.getInstance().connectToRepo(new URL("http://localhost:8080/B/atom11"), "A1",
				new UserCredentials("admin", "admin"));
		ctrl = CMISAccess.getInstance().createResourceController();
	}

	@Test
	public void testQueringFolders() {
		SearchController search = new SearchController(ctrl);

		List<IResource> folds = search.queringFolder("Fold");

		assertNotNull(folds);

		/*
		 * for (IResource folder : folds) { System.out.println(folder.getDisplayName());
		 * }
		 */
	}

	@Test
	public void testQueringDoc() {
		SearchController search = new SearchController(ctrl);

		List<IResource> docs = search.queringDoc("Document");

		assertNotNull(docs);

		/*
		 * for (IResource doc : docs) { System.out.println(doc.getDisplayName()); }
		 */
	}

	@Test
	public void testQueringDocContent() {
		SearchController search = new SearchController(ctrl);

		List<IDocument> docs = search.queringDocContent("At justo in urna");

		assertNotNull(docs);

		/*
		 * for (IDocument doc : docs) { System.out.println(doc.getDisplayName()); }
		 */

	}

	@After
	public void afterMethod() {
		ctrl.getSession().clear();
	}
}
