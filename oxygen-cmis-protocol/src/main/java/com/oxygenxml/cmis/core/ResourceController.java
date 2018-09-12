package com.oxygenxml.cmis.core;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;

public class ResourceController {

  private Session session;

  private static final String OBJ_TYPE = "cmis:document";

  /**
   * 
   * @param session
   */
  public ResourceController(Session session) {
    this.session = session;
  }

  /**
   * @return The root folder.
   */
  public Folder getRootFolder() {
    return session.getRootFolder();
  }

  /**
   * CREATE DOCUMENT METHOD
   * 
   * @param path
   * @param filename
   * @param content
   * @return
   * @throws UnsupportedEncodingException
   */
  public Document createDocument(Folder path, String filename, String content, String mimeType)
      throws UnsupportedEncodingException, CmisConstraintException {
    // TODO Pass a Reader instead of a String as content.

    String mimetype = mimeType.concat("; charset=UTF-8");

    byte[] contentBytes = content.getBytes("UTF-8");
    ByteArrayInputStream stream = new ByteArrayInputStream(contentBytes);

    ContentStream contentStream = session.getObjectFactory().createContentStream(filename, contentBytes.length,
        mimetype, stream);

    // prepare properties
    Map<String, Object> properties = new HashMap<String, Object>();

    properties.put(PropertyIds.NAME, filename);
    properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");

    // create the document
    try {
      return path.createDocument(properties, contentStream, VersioningState.NONE);
    } catch (Exception e2) {
      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + e2.getMessage());
    }
    return null;
  }

  /**
   * CREATE DOCUMENT METHOD
   * 
   * @param path
   * @param filename
   * @param content
   * @param versioningState
   * @return
   * @throws UnsupportedEncodingException
   *           Necessary VersionableType in order to get many versions
   */
  public Document createVersionedDocument(Folder path, String filename, String content, String mimetype,
      String objectType, VersioningState versioningState) throws UnsupportedEncodingException {

    byte[] contentBytes = content.getBytes("UTF-8");
    ByteArrayInputStream stream = new ByteArrayInputStream(contentBytes);

    ContentStream contentStream = session.getObjectFactory().createContentStream(filename, contentBytes.length,
        mimetype, stream);

    // prepare properties
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(PropertyIds.NAME, filename);

    // create the document
    Document document = null;
    try {
      properties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
      document = path.createDocument(properties, contentStream, versioningState);
    } catch (Exception e) {
      properties.put(PropertyIds.OBJECT_TYPE_ID, OBJ_TYPE);
      document = path.createDocument(properties, contentStream, versioningState);
    }
    return document;
  }

  /**
   * CREATE DOCUMENT METHOD with the Content stream
   * 
   * @param path
   * @param filename
   * @param content
   * @param versioningState
   * @return
   * @throws UnsupportedEncodingException
   *           Necessary VersionableType in order to get many versions
   */
  public Document createVersionedDocument(Folder path, String filename, ContentStream contentStream, String mimetype,
      String objectType, VersioningState versioningState) throws UnsupportedEncodingException {

    // prepare properties
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(PropertyIds.NAME, filename);

    // create the document
    Document document = null;
    try {
      properties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
      document = path.createDocument(properties, contentStream, versioningState);
    } catch (Exception e) {
      properties.put(PropertyIds.OBJECT_TYPE_ID, OBJ_TYPE);
      document = path.createDocument(properties, contentStream, versioningState);
    }
    return document;
  }

  /**
   * MOVE DOCUMENTE FROM SOURCE FOLDER TO TARGET FOLDER
   * 
   * @param sourceFolder
   * @param targetFolder
   * @param doc
   * @return
   */
  public boolean move(Folder sourceFolder, Folder targetFolder, Document doc) {
    return doc.move(sourceFolder, targetFolder) != null;
  }

  /**
   * ADD DOCUMENT TO FOLDER
   * 
   * @param folder
   * @param doc
   */
  public void addToFolder(Folder folder, Document doc) {
    doc.addToFolder(folder, true);
  }

  /**
   * REMOVE DOCUMENT FROM FOLDER
   * 
   * @param folder
   * @param doc
   */
  public void removeFromFolder(Folder folder, Document doc) {
    doc.removeFromFolder(folder);
  }

  /**
   * 
   * @param doc
   */
  public void deleteAllVersionsDocument(Document doc) {
    if (doc != null) {
      doc.delete(true);
    }
  }

  /**
   * DELETE ONE VERSION
   */
  public void deleteOneVersionDocument(Document doc) {
    if (doc != null) {
      doc.delete(false);
    }
  }

  /**
   * CREATE FOLDER
   * 
   * @param path
   * @param name
   * @return
   */
  public Folder createFolder(Folder parent, String name) {
    Map<String, Object> properties = new HashMap<String, Object>();

    properties.put(PropertyIds.NAME, name);
    properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");

    // create the folder
    return parent.createFolder(properties);
  }

  /**
   * DELETE
   * 
   * @param folder
   * @return
   */
  public List<String> deleteFolderTree(Folder folder) {
    if (folder != null) {
      return folder.deleteTree(true, UnfileObject.DELETE, true);
    }

    return null;
  }

  /**
   * RENAME
   * 
   * @param folder
   * @param newName
   * @return
   */
  public CmisObject renameFolder(Folder folder, String newName) {
    return folder.rename(newName);
  }

  /**
   * GET DOC
   * 
   * @param id
   * @return
   */
  public Document getDocument(String id) {
    return (Document) session.getObject(id);
  }

  /**
   * GET DOC
   * 
   * @param id
   * @return
   */
  public Folder getFolder(String id) {
    return (Folder) session.getObject(id);
  }

  public Session getSession() {
    return session;
  }

  /**
   * GET DOCUMENT CONTENT
   * 
   * @param docID
   * @return
   * @throws UnsupportedEncodingException
   */
  public Reader getDocumentContent(String docID) throws UnsupportedEncodingException {
    Document document = null;
    if (docID != null) {
      try {
        document = (Document) session.getObject(docID);
      } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException e) {
        System.out.println("No object found");
      }
      if (document != null) {
        ContentStream contentStream = document.getContentStream();

        java.io.InputStream stream = contentStream.getStream();

        // TODO Get the encoding dynamically.
        return new InputStreamReader(stream, "UTF-8");
      }
    }
    return null;
  }

  public CmisObject getCmisObj(String objectID) {
    return session.getObject(objectID);
  }

}
