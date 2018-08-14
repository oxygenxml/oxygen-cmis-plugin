package com.oxygenxml.cmis.core;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

public class ConnectionTestBase {

  /**
   * Tests is a document exists in the parent folder.
   * 
   * @param document
   * @param folder
   * @return
   */
  protected boolean documentExists(Document document, Folder folder) {
    for (CmisObject child : folder.getChildren()) {
      if (child instanceof Document) {
        if (document.getName().equals(child.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean folderExists(Folder folder, Folder rootFolder) {
    for (CmisObject child : rootFolder.getChildren()) {
      if (child instanceof Document) {
        if (folder.getName().equals(child.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 
   * @param folder
   * @return String ID or null
   */
  protected String getFirstDocId(Folder folder) {
    for (CmisObject child : folder.getChildren()) {
      if (child instanceof Document) {
        return child.getId();
      }
    }
    return null;
  }

  /**
   * 
   * @param docContent
   * @return String content of the document
   * @throws IOException
   */
  protected String read(Reader docContent) throws IOException {
    StringBuilder b = new StringBuilder();
    try {
      char[] c = new char[1024];
      int l = -1;
      while ((l = docContent.read(c)) != -1) {
        b.append(c, 0, l);
      }
    } finally {
      docContent.close();
    }
    return b.toString();
  }

  protected void debugPrint(Folder folder) {
    System.out.println(folder.getName());
    for (CmisObject child : folder.getChildren()) {
      if (child instanceof Document) {
        System.out.println("  (Doc) Name: " + child.getName() + " & Id: " + child.getId());
      } else if (child instanceof Folder) {
        System.out.println("  (Folder) Name: " + child.getName() + " & Id: " + child.getId());
      }
    }
  }

  /*
   * Document scavenger
   */

  // Create a memory copy of the documents
  private Map<String, Set<String>> createdDocs = new HashMap<>();

  // Put docs to the memory copy using parent path (folder)
  protected Document createDocument(Folder parent, String docName, String content) throws UnsupportedEncodingException {
    ResourceController ctrl = CMISAccess.getInstance().createResourceController();

    Set<String> docs = createdDocs.get(parent.getPath());
    if (docs == null) {
      docs = new HashSet<>();
      createdDocs.put(parent.getPath(), docs);
    }
    docs.add(docName);

    return ctrl.createVersionedDocument(parent, docName, content, VersioningState.MINOR);
  }

  /*
   * Cleanup docs
   */
  protected void cleanUpDocuments() {
    ResourceController ctrl = CMISAccess.getInstance().createResourceController();

    // Get the keys
    Set<String> keySet = createdDocs.keySet();

    for (String path : keySet) {

      // Get the path of the parent folder
      Folder folder = (Folder) ctrl.getSession().getObjectByPath(path);
      Set<String> docs = createdDocs.get(path);
      // Iterate over children
      ItemIterable<CmisObject> children = folder.getChildren();

      // Check if there is an item already
      for (CmisObject cmisObject : children) {
        if (docs.contains(cmisObject.getName())) {
          cmisObject.delete();
        }
      }
    }
  }

  /*
   * Folder scavenger
   */

  // Create a memory copy of the folders
  private Map<String, Set<String>> createdFolders = new HashMap<>();

  // Put the folders to the memory copy using parent path (folder)
  protected Folder createFolder(Folder parent, String name) throws UnsupportedEncodingException {
    ResourceController ctrl = CMISAccess.getInstance().createResourceController();

    Set<String> folders = createdFolders.get(parent.getPath());
    if (folders == null) {
      folders = new HashSet<>();
      createdFolders.put(parent.getPath(), folders);
    }
    folders.add(name);

    return ctrl.createFolder(parent, name);
  }

  /*
   * Cleanup folders
   */
  protected void cleanUpFolders() {
    ResourceController ctrl = CMISAccess.getInstance().createResourceController();

    // Get the keys
    Set<String> keySet = createdFolders.keySet();
    for (String path : keySet) {

      // Get the path of the parent folder
      Folder folder = (Folder) ctrl.getSession().getObjectByPath(path);

      // Iterate over children
      Set<String> folders = createdFolders.get(path);
      ItemIterable<CmisObject> children = folder.getChildren();

      // Check if there is an item already
      for (CmisObject cmisObject : children) {
        if (folders.contains(cmisObject.getName())) {
          cmisObject.delete();
        }
      }
    }
  }

	/**
	 * Helper method to get CmisObject from URL
	 * 
	 * @param url
	 * @return
	 * @throws IOException 
	 */
	public CmisObject getObjectFromURL(String url, String serverUrl, UserCredentials credentials) throws IOException {
		if (url == null) {
			throw new NullPointerException();
		}
		CmisURLConnection cuc = new CmisURLConnection(new URL(serverUrl), CMISAccess.getInstance());
		cuc.setCredentials(new UserCredentials("admin", "admin"));
		
		return cuc.getCMISObject(url);
	}
  
  
}
