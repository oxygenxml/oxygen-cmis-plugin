package com.oxygenxml.cmis.core;

import static org.junit.Assert.*;

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
}
