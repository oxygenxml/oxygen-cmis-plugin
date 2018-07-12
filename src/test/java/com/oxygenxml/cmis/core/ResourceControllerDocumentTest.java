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
import org.junit.Test;

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
    testFolder = ctrl.createFolder(ctrl.getRootFolder(), "testFolder_DocTests");
  }

  /**
   * Tests the deletion of a document.
   * 
   * @throws IOException If it fails.
   */
  @Test(timeout=10000)
  public void testDocumentDelete() throws IOException {
    Document document = ctrl.createDocument(testFolder, "test1.txt", "test content");
    debugPrint(testFolder);
    // TODO Alexey Assert that the new document exists before deleting it.
    //MAYBE DONE
    if(documentExists(document, testFolder)) {
      ctrl.deleteAllVersionsDocument(document);
    }
    
    debugPrint(testFolder);
    
    Assert.assertFalse(documentExists(document, testFolder));
  }

  @Test(timeout=10000)
  public void testMoveDocument() throws IOException {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(PropertyIds.NAME, "testFolder1");
    properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder"); 

    Folder sourceFolder = testFolder;
    Folder targetFolder = testFolder.createFolder(properties);
    debugPrint(testFolder);
    Document document = ctrl.createDocument(testFolder, "test1.txt", "test content");
    debugPrint(testFolder);
    ctrl.move(sourceFolder, targetFolder, document);
    debugPrint(targetFolder);
    
    // TODO Alexey Doesn't move.
    //
    Assert.assertTrue("The file wasn't moved", documentExists(document, targetFolder));
  }

  @Test(timeout=10000)
  public void testDocumentContent() throws IOException {
    // TODO Alexey pass the correct ID.
    //MAYBE DONE
    Folder folder = ctrl.getRootFolder();
    String docId = getFirstDocId(folder);
    Reader docContent = ctrl.getDocumentContent(docId);
    assertEquals("", read(docContent));
  }

  
  
  @After
  public void afterMethod(){
    if (testFolder != null) {
      ctrl.deleteFolderTree(testFolder);
    }
  }
}
