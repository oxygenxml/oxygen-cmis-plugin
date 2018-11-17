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
}
