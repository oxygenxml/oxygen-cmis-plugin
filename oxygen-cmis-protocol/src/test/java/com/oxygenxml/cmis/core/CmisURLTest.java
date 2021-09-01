package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

public class CmisURLTest {
  
  /**
   * <p><b>Description:</b> Test that the extension is computed properly.</p>
   *
   * @author cristi_talau
   *
   * @throws Exception
   */
  @Test
  public void testExtension() throws Exception {
    CmisURL cmisURL = CmisURL.parse(
        "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/B/file.txt");
    assertEquals("txt", cmisURL.getExtension());
    
    // No ext
    cmisURL = CmisURL.parse(
        "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/B/file");
    assertEquals("", cmisURL.getExtension());

    // No name
    cmisURL = CmisURL.parse(
        "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/B/folder/.gitignore");
    assertEquals("gitignore", cmisURL.getExtension());

    // Folder
    cmisURL = CmisURL.parse(
        "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/B/folder/");
    assertEquals("", cmisURL.getExtension());
  }
  
  /**
   * <p><b>Description:</b> Test that the external form is computed properly.</p>
   *
   * @author cristi_talau
   *
   * @throws Exception
   */
  @Test
  public void testExteralForm() throws Exception {
    // Normal case
    String cmisUrlStr = "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/B/file.txt";
    assertEquals(cmisUrlStr, CmisURL.parse(cmisUrlStr).toExternalForm());

    // Percent-encoded path components.
    cmisUrlStr = "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/B%201/folder%201/folder%202/file%201.txt";
    assertEquals(cmisUrlStr, CmisURL.parse(cmisUrlStr).toExternalForm());
    
    // Repo URL
    cmisUrlStr = "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/B%201/";
    assertEquals(cmisUrlStr, CmisURL.parse(cmisUrlStr).toExternalForm());

  }
  
  /**
   * <p><b>Description:</b> Test that the server URL is parsed correctly.</p>
   *
   * @author cristi_talau
   *
   * @throws Exception
   */
  @Test
  public void testParseServerUrl() throws Exception {
    // Normal case
    String cmisUrlStr = "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/";
    assertEquals("http://localhost:8080/B/atom11", CmisURL.parseServerUrl(cmisUrlStr).toExternalForm());
  }
  
 
  @Test
  public void externalFormWithRepoNameIsParsedCorrectly() throws Exception {
    final URL serverUrl = new URL("http://10.0.0.179/my/personal/syncro/_vti_bin/cmis/rest?getRepositories"); 
    final String repoId = "78ad70d4-396c-4a1e-9438-f38f22510b1d";
    final String repoName = "Repository Name";
    
    CmisURL cmisUrl = CmisURL.ofRepoWithName(serverUrl, new RepositoryInfo(repoId, repoName));
    String externalForm = cmisUrl.toExternalForm();
    // the encoded external form for the CMIS
    //cmis://http%3A%2F%2F10.0.0.179%2Fmy%2Fpersonal%2Fsyncro%2F_vti_bin%2Fcmis%2Frest%3FgetRepositories/Repository%20Name%20%5B78ad70d4-396c-4a1e-9438-f38f22510b1d%5D/
    
    CmisURL parsedCmisUrl = CmisURL.parse(externalForm);
    assertEquals(repoId, parsedCmisUrl.getRepositoryId());
    assertEquals(serverUrl, parsedCmisUrl.getServerHttpUrl());
    assertEquals(repoName, parsedCmisUrl.getRepositoryInfo().getName());
  }

}
