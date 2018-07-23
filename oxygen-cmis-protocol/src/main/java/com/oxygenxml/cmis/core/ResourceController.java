package com.oxygenxml.cmis.core;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

public class ResourceController {
  
  private Session session;
  
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
   * @param path
   * @param filename
   * @param content
   * @return
   * @throws UnsupportedEncodingException
   */
  public Document createDocument(
      Folder path, 
      String filename, 
      String content) throws UnsupportedEncodingException {
    
    // TODO Pass a Reader instead of a String as content.
   
    String mimetype = "text/plain; charset=UTF-8";

    byte[] contentBytes = content.getBytes("UTF-8");
    ByteArrayInputStream stream = new ByteArrayInputStream(contentBytes);

    ContentStream contentStream = session.getObjectFactory().createContentStream(filename, contentBytes.length,
        mimetype, stream);

    // prepare properties
    Map<String, Object> properties = new HashMap<String, Object>();
    
    properties.put(PropertyIds.NAME, filename);
    properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");

    // create the document
    return path.createDocument(properties, contentStream, VersioningState.NONE);
  }
  /**
   * CREATE DOCUMENT METHOD
   * @param path
   * @param filename
   * @param content
   * @param versioningState
   * @return
   * @throws UnsupportedEncodingException
   * Necessary VersionableType in order to get many versions 
   */
  public Document createVersionedDocument(
      Folder path, 
      String filename, 
      String content,
      VersioningState versioningState) throws UnsupportedEncodingException {
    
    // TODO Pass a Reader instead of a String as content.
   
    String mimetype = "text/plain; charset=UTF-8";

    byte[] contentBytes = content.getBytes("UTF-8");
    ByteArrayInputStream stream = new ByteArrayInputStream(contentBytes);

    ContentStream contentStream = session.getObjectFactory().createContentStream(filename, contentBytes.length,
        mimetype, stream);

    // prepare properties
    Map<String, Object> properties = new HashMap<String, Object>();
    
    properties.put(PropertyIds.NAME, filename);
    properties.put(PropertyIds.OBJECT_TYPE_ID, "VersionableType");
    properties.put(PropertyIds.VERSION_LABEL, null);
    properties.put(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null);

    // create the document
    return path.createDocument(properties, contentStream, versioningState);
  }
  
  /**
   * MOVE DOCUMENTE FROM SOURCE FOLDER TO TARGET FOLDER
   * @param sourceFolder
   * @param targetFolder
   * @param doc
   * @return
   */
 public  boolean move(Folder sourceFolder, Folder targetFolder, Document doc) {
    return doc.move(sourceFolder, targetFolder) != null;
  }

 /**
  * ADD DOCUMENT TO FOLDER
  * @param folder
  * @param doc
  */
 public void addToFolder(Folder folder, Document doc) {
    doc.addToFolder(folder, true);
  }

 /**
  * REMOVE DOCUMENT FROM FOLDER
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
 public  void deleteAllVersionsDocument(Document doc) {
    doc.delete(true);
  }

 /**
  * DELETE ONE VERSION
  */
 public void deleteOneVersionDocument(Document doc) {
    doc.delete(false);
  }
 
 /**
  * CREATE FOLDER
  * @param path
  * @param name
  * @return
  */
 public Folder createFolder(Folder path, String name) {
   Map<String, Object> properties = new HashMap<String, Object>();

   properties.put(PropertyIds.NAME, name);
   properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");

   Folder parent = null;
   parent = path;
   
   // create the folder
   return parent.createFolder(properties);
 }
 
 /**
  * DELETE
  * @param folder
  * @return
  */
 public List<String> deleteFolderTree(Folder folder) {
   return folder.deleteTree(true, UnfileObject.DELETE, true);
 }

 /**
  * RENAME
  * @param folder
  * @param newName
  * @return
  */
 public CmisObject renameFolder(Folder folder, String newName) {
    return folder.rename(newName);
 }
 
 /**
  * GET DOC
  * @param id
  * @return
  */
 public Document getDocument(String id) {
   return (Document) session.getObject(id);
 }
 
 /**
  * GET DOC
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
  * @param docID
  * @return
  * @throws UnsupportedEncodingException
  */
 public Reader getDocumentContent(String docID) throws UnsupportedEncodingException {
   Document document = (Document) session.getObject(docID);
   ContentStream contentStream = document.getContentStream();
   
   List<Property<?>> properties = document.getProperties();
   for (Property<?> property : properties) {
     System.out.println(property.getDisplayName() + " -> " + property.getValueAsString());
   }
   
   java.io.InputStream stream = contentStream.getStream();
   
   // TODO Get the encoding dynamically.
   return new InputStreamReader(stream, "UTF-8");    
 }

public CmisObject getCmisObj(String objectID) {
  return session.getObject(objectID);
}
}
