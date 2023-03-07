package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.action.CmisCheckIn;
import com.oxygenxml.cmis.web.action.CmisCheckOut;

import ro.sync.basic.io.IOUtil;

/**
 * @author mihaela
 */
public class CmisCheckedOutContentIT {

	@Rule
	public CmisAccessProvider cmisAccessProvider = new CmisAccessProvider();
	private ResourceController ctrl;

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		ctrl = cmisAccessProvider.getCmisAccess().createResourceController();
	}

	/**
	 * WA-6316
	 * @throws Exception
	 */
	@Test
	public void testContentAfterCheckIn() throws Exception {
	  Document document = null;

	  try {
	    String fileName = "docInStream.xml";
      String initialContent = "<initial/>";
      document = createDocument(fileName, initialContent);

	    URL url = new URL(CmisURLConnection.generateURLObject(ctrl.getRootFolder(), document, ctrl));

	    String modifiedContent = "<modified/>";
	    CmisCheckOut.checkOutDocument(document);
      writeContentToDoc(url, modifiedContent);
	    
	    CmisCheckIn.checkInDocument(document, "minor", "some minor version");
	    checkDocumentContent(url, modifiedContent);
	    
	    document.refresh();
	    checkDocumentContent(url, modifiedContent);
	    
	    document = document.getObjectOfLatestVersion(false);
	    CmisCheckOut.checkOutDocument(document);
	    checkDocumentContent(url, modifiedContent);
	  } finally {
	    if(document != null) {
	      ctrl.deleteAllVersionsDocument(document);
	      document = null;
	    }
	  }
	}

  private Document createDocument(String fileName, String initialContent) {
    Document document;
    document = ctrl.createVersionedDocument(
        ctrl.getRootFolder(), 
        fileName, 
        ctrl.createXmlUtf8ContentStream(fileName, initialContent), 
        ResourceController.VERSIONABLE_OBJ_TYPE, 
        VersioningState.MINOR);
    return document;
  }

  private void checkDocumentContent(URL url, String expectedContent) throws IOException {
    CmisURLConnection rConnection = cmisAccessProvider.createConnection(url);
    try (InputStream inputStream = rConnection.getInputStream()) {
    	byte[] bytes = IOUtil.readBytes(inputStream);
    	assertEquals(expectedContent, new String(bytes, StandardCharsets.UTF_8));
    }
  }

  private void writeContentToDoc(URL url, String content) throws IOException {
    CmisURLConnection wConnection = cmisAccessProvider.createConnection(url);
    wConnection.setDoOutput(true);
    
    InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    try (OutputStream outputStream = wConnection.getOutputStream()) {
    	IOUtils.copy(in, outputStream);
    }
  }
}
