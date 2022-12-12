package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
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

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.action.CmisCheckIn;
import com.oxygenxml.cmis.web.action.CmisCheckOut;

import ro.sync.basic.io.IOUtil;

public class CmisGetLatestContentStreamIT {

	@Rule
	public CmisAccessProvider cmisAccessProvider = new CmisAccessProvider();
	private ResourceController ctrl;

	@Before
	public void setUp() throws Exception {
		CMISAccess cmisAccess = cmisAccessProvider.getCmisAccess();
		ctrl = cmisAccess.createResourceController();
	}

	@Test
	public void testGettingLatestInputStream() throws Exception {
		
		Document document = null;
		
		for(int i = 0; i < 10; i++) {
			try {
				String filename = "docInStream" + i + ".xml";
        document = ctrl.createVersionedDocument(
            ctrl.getRootFolder(), 
            filename, 
				    ctrl.createXmlUtf8ContentStream(filename, "<html/>"), 
				    ResourceController.VERSIONABLE_OBJ_TYPE, 
				    VersioningState.MINOR);
		
				URL url = new URL(CmisURLConnection.generateURLObject(ctrl.getRootFolder(), document, ctrl));
				
				CmisCheckOut.checkOutDocument(document);
				document.refresh();
				assertTrue(document.isVersionSeriesCheckedOut());
				
				CmisCheckIn.checkInDocument(document, "minor", "some minor version");
				document = document.getObjectOfLatestVersion(false);
				assertFalse(document.isVersionSeriesCheckedOut());
				
				CmisURLConnection wConnection = cmisAccessProvider.createConnection(url);
				wConnection.setDoOutput(true);
				
				InputStream in = new ByteArrayInputStream("<root/>".getBytes(StandardCharsets.UTF_8));
				try (OutputStream outputStream = wConnection.getOutputStream()) {
					IOUtils.copy(in, outputStream);
				}
		
				CmisURLConnection rConnection = cmisAccessProvider.createConnection(url);
				try (InputStream inputStream = rConnection.getInputStream()) {
					byte[] bytes = IOUtil.readBytes(inputStream);
					assertEquals("<root/>", new String(bytes, StandardCharsets.UTF_8));
				}
			} finally {
				if(document != null) {
					ctrl.deleteAllVersionsDocument(document);
					document = null;
				}
			}
		}
	}
}
