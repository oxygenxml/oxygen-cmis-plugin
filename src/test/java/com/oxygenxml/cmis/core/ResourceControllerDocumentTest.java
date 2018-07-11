package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.oxygenxml.cmis.core.model.impl.FolderImpl;

/**
 * Tests for operations over resources.
 * 
 */
public class ResourceControllerDocumentTest extends ConnectionTestBase {
  /**
   * Root folder.
   */
  private Folder testFolder;
  /**
   * Executes operations over the resources.
   */
  private ResourceController ctrl;

  @Before
  public void setUp() throws Exception {
    CMISAccess.getInstance().connect(new URL("http://localhost:8080/atom11"), "A1");

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(PropertyIds.NAME, "testFolder");
    properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");

    ctrl = CMISAccess.getInstance().createResourceController();
    
    // Create a folder to keep the new documents.
    testFolder = ctrl.createFolder(((FolderImpl) ctrl.getRootFolder()).getFolder(), "testFolder_DocTests");
  }

  /**
   * Tests the deletion of a document.
   * 
   * @throws IOException If it fails.
   */
  @org.junit.Test
  public void testDocumentDelete() throws IOException {
    Document document = ctrl.createDocument(testFolder, "test1.txt", "test content");
    
    // TODO Alexey Assert that the new document exists before deleting it.
    ctrl.deleteAllVersionsDocument(document);

    Assert.assertFalse(documentExists(document, testFolder));
  }

  @org.junit.Test
  public void testMoveDocument() throws IOException {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(PropertyIds.NAME, "testFolder1");
    properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder"); 

    Folder sourceFolder = testFolder;
    Folder targetFolder = testFolder.createFolder(properties);

    Document document = ctrl.createDocument(testFolder, "test1.txt", "test content");
    ctrl.move(sourceFolder, targetFolder, document);

    // TODO Alexey Doesn't move.
    Assert.assertTrue("The folder wasn't moved", documentExists(document, targetFolder));
  }

  @org.junit.Test
  public void testDocumentContent() throws IOException {
    // TODO Alexey pass the correct ID.
    Reader docContent = ctrl.getDocumentContent("ID");

    assertEquals("", read(docContent));
  }

  
  
  @After
  public void afterMethod(){
    ctrl.deleteFolderTree(testFolder);
  }
}
