package com.oxygenxml.cmis.core.urlhandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ConnectionTestBase;
import com.oxygenxml.cmis.core.ResourceController;

/**
 * Tests for accessing CMIS resources through our custom protocol. 
 */
public class CustomProtocolTest extends ConnectionTestBase {

  /**
	 * TODO Code review. It makes no sense to keep the document as a member variable
	 * since it is used only in each test. A local variable in each test would
	 * suffice. A member variable makes sense if we always create it on the setUp()
	 * and we delete it on the afterMethod().
   */
  private Folder root;
  private ResourceController ctrl;

  /**
   * CONECTION TO SERVER REPOSITORY, ACCES ROOT FOLDER
   * 
   * @throws MalformedURLException
   */
  @Before
  public void setUp() throws MalformedURLException {
    CMISAccess.getInstance().connectToRepo(new URL("http://localhost:8080/B/atom11"), "A1");
    ctrl = CMISAccess.getInstance().createResourceController();
    root = ctrl.getRootFolder();
  }
  
  @Test
  public void testGenerateURLObject() throws UnsupportedEncodingException {
    Document doc = null;

      doc = createDocument(root, "urlDoc", "some text");
      
      String url = CmisURLExtension.getCustomURL(doc, ctrl);
      
			assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/urlDoc", url);
      
			System.out.println("[cmis:document] id = " + doc.getId() + " URL = " + url);
      
      assertNotNull(url);
      
  }

  @Test
  public void testGetObjectFromURL() throws UnsupportedEncodingException, MalformedURLException {
    Document doc = null;

      doc = createDocument(root, "url", "some text");
 
      CmisURLExtension cpe = new CmisURLExtension();
      
      String url = CmisURLExtension.getCustomURL(doc, ctrl);
      
      // TODO Code review. Assert the obtained URL.
			assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/url", url);
      
			System.out.println("[cmis:document] id = " + doc.getId() + " URL = " + url);
      
      Document docURL = (Document) new CmisURLConnection(new URL(url), CMISAccess.getInstance()).getCMISObject(url);
      
      assertNotNull(docURL);
      assertEquals(doc.getName(), docURL.getName());

  }
  
/*   @Test
  public void testGetDocumentContent() throws IOException, UnsupportedEncodingException, MalformedURLException {
    Document doc = null;
    

      doc = createDocument(root, "urlDocCont", "some test text");
      
      CustomProtocolExtension cpe = new CustomProtocolExtension();
      
      String url = CustomProtocolExtension.getCustomURL(doc, ctrl);
      
      // TODO Code review. Assert the obtained URL.
			assertEquals("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/urlDocCont", url);
      
			System.out.println("[cmis:document] id = " + doc.getId() + " URL = " + url);
      
      Reader docContent = cpe.getContentURL(url, ctrl);
      
      assertNotNull(docContent);
      assertEquals("some test text", read(docContent));
      

  }
	}*/
  
  @Test
  public void testGetFolderFromURL() throws MalformedURLException, UnsupportedEncodingException {
    Folder folder = null;

      folder = createFolder(root, "folderURL");
      
      CmisURLExtension cpe = new CmisURLExtension();
      
      String url = CmisURLExtension.getCustomURL(folder, ctrl);
      
      
			System.out.println("[cmis:folder] id = " + folder.getId() + " URL = " + url);
      
      Folder foldURL = (Folder) new CmisURLConnection(new URL(url), CMISAccess.getInstance()).getCMISObject(url);
      
      assertNotNull(foldURL);
      assertEquals("folderURL", foldURL.getName());

  }
  
  @After
  public void afterMethod(){   
    cleanUpDocuments();
    cleanUpFolders();
    ctrl.getSession().clear();
  }
}
