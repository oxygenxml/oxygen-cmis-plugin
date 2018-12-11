package com.oxygenxml.cmis.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;

public class ResourceController {
  /**
   * Default object type.
   */
  public static final String DEFAULT_OBJ_TYPE = "cmis:document";

  /**
   * Type of the versionable objects.
   */
  public static final String VERSIONABLE_OBJ_TYPE = "VersionableType";
  
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(ResourceController.class);

  /**
   * The CMIS session.
   */
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
   * 
   * @param path
   * @param filename
   * @param content
   * @return
   * @throws UnsupportedEncodingException
   */
  public Document createDocument(Folder path, String filename, String content, String mimeType) {
    // TODO Pass a Reader instead of a String as content.

    String mimetype = mimeType.concat("; charset=UTF-8");

    byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
    ByteArrayInputStream stream = new ByteArrayInputStream(contentBytes);

    ContentStream contentStream = session.getObjectFactory().createContentStream(filename, contentBytes.length,
        mimetype, stream);

    // prepare properties
    Map<String, Object> properties = new HashMap<>();

    properties.put(PropertyIds.NAME, filename);
    properties.put(PropertyIds.OBJECT_TYPE_ID, DEFAULT_OBJ_TYPE);

    // create the document
    return path.createDocument(properties, contentStream, VersioningState.NONE);
  }

  /**
   * USE THIS METHOD ONLY IN TESTS. IT DOES NOT TAKE ENCODING INTO ACCOUNT!
   */
  @VisibleForTesting
  Document createVersionedDocument(Folder path, String filename, String content, String mimetype,
      String objectType, VersioningState versioningState) {

    byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
    ByteArrayInputStream stream = new ByteArrayInputStream(contentBytes);

    ContentStream contentStream = session.getObjectFactory().createContentStream(filename, contentBytes.length,
        mimetype, stream);

    return createVersionedDocument(path, filename, contentStream, objectType, versioningState);
  }

  /**
   * Create a new empty document. 
   *
   * @param path The path where to file the document.
   * @param filename The name of the file.
   * @param mimetype The mime type of the document.
   * @param versioningState The versioning state.
   * 
   * @return The document.
   */
  public Document createEmptyVersionedDocument(
      Folder path, String filename, String mimetype, VersioningState versioningState) {
    String content = "<empty/>";
    ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.US_ASCII));
    ContentStream contentStream = session.getObjectFactory().createContentStream(
        filename, content.length(), mimetype, stream);
    return createVersionedDocument(path, filename, contentStream, VERSIONABLE_OBJ_TYPE, versioningState);
  }

  /**
   * Create an UTF-8 XML content stream for the given file.
   * @param fileName The file name.
   * @param content The file content, needs to be ASCII.
   * @return The content stream.
   */
  public ContentStream createXmlUtf8ContentStream(String fileName, String content) {
    ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    return session.getObjectFactory().createContentStream(
        fileName, content.length(), "text/xml", stream);
  }

  /**
   * Create a document based on the given content stream.
   * 
   * @param path The path where to create the document.
   * @param filename The name of the file.
   * @param content The content stream.
   * @param versioningState The versioning state.
   * 
   * @return The created document.
   */
  public Document createVersionedDocument(Folder path, String filename, ContentStream contentStream, String objectType,
      VersioningState versioningState) {
    Map<String, Object> properties = new HashMap<>();
    properties.put(PropertyIds.NAME, filename);
    if (!isTypeSupported(objectType)) {
      // Fallback to the default object type.
      objectType = DEFAULT_OBJ_TYPE;
    }
    properties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
    return path.createDocument(properties, contentStream, versioningState);
  }

  /**
   * @param The type to check.
   * @return <code>true</code> if the type is supported.
   */
  @VisibleForTesting
  boolean isTypeSupported(String objectType) {
    boolean typeExists = true;
    if (!DEFAULT_OBJ_TYPE.equals(objectType)) {
      try {
        session.getTypeDefinition(objectType);
      } catch (CmisObjectNotFoundException e) {
        typeExists = false;
      }
    }
    return typeExists;
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
    Map<String, Object> properties = new HashMap<>();

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

    return Collections.emptyList();
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
  public Reader getDocumentContent(String docID) {
    Document document = null;
    if (docID != null) {
      try {
        document = (Document) session.getObject(docID);
      } catch (CmisObjectNotFoundException e) {
        logger.warn("No object found for: " + docID);
      }
      
      if (document != null) {
        ContentStream contentStream = document.getContentStream();

        InputStream stream = contentStream.getStream();

        // TODO Get the encoding dynamically.
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
      }
    }
    return null;
  }

  public CmisObject getCmisObj(String objectID) {
    return session.getObject(objectID);
  }

}
