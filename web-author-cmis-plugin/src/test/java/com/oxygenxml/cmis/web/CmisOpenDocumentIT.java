package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

public class CmisOpenDocumentIT {

  @Rule
  public CmisAccessProvider cmisAccessProvider = new CmisAccessProvider();

  /**
   * <p><b>Description:</b> Test missing document throws FileNotFound.</p>
   * <p><b>Bug ID:</b> WA-3208</p>
   *
   * @author cristi_talau
   *
   * @throws Exception
   */
  @Test
  public void testOpenMissingDocument() throws Exception {
    URL url = new URL("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/new_file_WA_3208.xml");
    CmisURLConnection wConnection = cmisAccessProvider.createConnection(url);
    CmisBrowsingURLConnection cmisBrowsingURLConnection = new CmisBrowsingURLConnection(wConnection, url);
    
    try {
      cmisBrowsingURLConnection.getInputStream();
      fail("Connection to missing file should fail");
    } catch (FileNotFoundException e) {
      // expected
      assertEquals("/A1/new_file_WA_3208.xml", e.getMessage());
    }
  }

}
