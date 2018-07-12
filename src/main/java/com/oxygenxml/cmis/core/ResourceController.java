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

  public ResourceController(Session session) {
    this.session = session;
  }
  
  /**
   * @return The root folder.
   */
  public Folder getRootFolder() {
    return session.getRootFolder();
  }
  
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
  
 public  boolean move(Folder sourceFolder, Folder targetFolder, Document doc) {
    return doc.move(sourceFolder, targetFolder) != null;
  }

 public void addToFolder(Folder folder, Document doc) {
    doc.addToFolder(folder, true);
  }

 public void removeFromFolder(Folder folder, Document doc) {
    doc.removeFromFolder(folder);
  }

 public  void deleteAllVersionsDocument(Document doc) {
    doc.delete(true);
  }

 public void deleteOneVersionDocument(Document doc) {
    doc.delete(false);
  }
 
 public Folder createFolder(Folder path, String name) {
   Map<String, Object> properties = new HashMap<String, Object>();

   properties.put(PropertyIds.NAME, name);
   properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");

   Folder parent = null;
   parent = path;
   
   // create the folder
   return parent.createFolder(properties);
 }
 
 public List<String> deleteFolderTree(Folder folder) {
   return folder.deleteTree(true, UnfileObject.DELETE, true);
 }

 public CmisObject renameFolder(Folder folder, String newName) {
    return folder.rename(newName);
 }
 
 
 public Document getDocument(String id) {
   return (Document) session.getObject(id);
 }
 
 public Session getSession() {
   return session;
 }
 
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
 
}
