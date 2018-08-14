package com.oxygenxml.cmis.core;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;

import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

public class SearchController {

  public static final int SEARCH_IN_DOCUMENT = 1;
  public static final int SEARCH_IN_FOLDER = 2;

  private ResourceController ctrl;
  private Scanner scanner;

  /**
   * CONSTRUCTOR
   * 
   * @param RESOURCE
   *          CONTROLER ctrl
   */
  public SearchController(ResourceController ctrl) {
    this.ctrl = ctrl;

  }

  /**
   * 
   * 
   * @param cmisType
   * @param name
   * @param oc
   */
  private List<IResource> queryResourceName(String name, int searchObjectTypes) {
    ArrayList<IResource> resources = new ArrayList<>();

    OperationContext oc = ctrl.getSession().createOperationContext();
    // oc.setFilterString("cmis:objectId,cmis:name,cmis:createdBy");
    String scope = "";

    if ((searchObjectTypes & SEARCH_IN_DOCUMENT) != 0) {
      scope = "cmis:document";
    }

    else if ((searchObjectTypes & SEARCH_IN_FOLDER) != 0) {
      if (scope.length() > 0) {
        scope += ",";
      }
      scope += "cmis:folder";
    }

    ItemIterable<CmisObject> results = ctrl.getSession().queryObjects(scope, "cmis:name LIKE '%" + name + "%'", false,
        oc);

    for (CmisObject cmisObject : results) {
      IResource res = null;
      if (cmisObject instanceof Document) {
        res = new DocumentImpl((Document) cmisObject);
      } else {
        res = new FolderImpl((Folder) cmisObject);
      }

      resources.add(res);
    }

    return resources;
  }

  /**
   * 
   * 
   * @param cmisType
   * @param name
   * @param oc
   */
  private List<IResource> queryResource(String content, int searchObjectTypes) {
    ArrayList<IResource> resources = new ArrayList<>();

    OperationContext oc = ctrl.getSession().createOperationContext();
    String scope = "";

    if ((searchObjectTypes & SEARCH_IN_DOCUMENT) != 0) {
      scope = "cmis:document";
    }

    else if ((searchObjectTypes & SEARCH_IN_FOLDER) != 0) {
      if (scope.length() > 0) {
        scope += ",";
      }
      scope += "cmis:folder";
    }

    ItemIterable<CmisObject> results = ctrl.getSession().queryObjects(scope,
        "cmis:name LIKE '%" + content + "%' OR cmis:description LIKE '%" + content + "%' OR cmis:createdBy LIKE '%" + content + "%'", false, oc);

    for (CmisObject cmisObject : results) {
      IResource res = null;
      if (cmisObject instanceof Document) {
        res = new DocumentImpl((Document) cmisObject);
      } else {
        res = new FolderImpl((Folder) cmisObject);
      }

      resources.add(res);
    }

    return resources;
  }

  /**
   * Get the results for searching the name in the documents
   * 
   * @param content
   * @return
   */
  public List<IResource> queryDocName(String content) {
    return queryResourceName(content, SEARCH_IN_DOCUMENT);
  }

  /**
   * Get the results for searching the name in the folders
   * 
   * @param content
   * @return
   */
  public List<IResource> queryFolderName(String content) {
    return queryResourceName(content, SEARCH_IN_FOLDER);
  }

  /**
   * Get the results for searching the name and the title in the documents
   * 
   * @param content
   * @return
   */
  public List<IResource> queryDoc(String content) {
    return queryResource(content, SEARCH_IN_DOCUMENT);
  }

  /**
   * Get the results for searching the name and the title in the folders
   * 
   * @param content
   * @return
   */
  public List<IResource> queryFolder(String content) {
    return queryResource(content, SEARCH_IN_FOLDER);
  }

  /**
   * METHOD TO SEARCH DOCUMENTS WITH SPECIFIC CONTENT!
   * 
   * @param content
   * @return
   */
  public List<IDocument> queryDocContent(String content) {
    ArrayList<IDocument> docList = new ArrayList<IDocument>();

    OperationContext oc = ctrl.getSession().createOperationContext();
    // oc.setFilterString("cmis:objectId,cmis:name,cmis:createdBy");
    ItemIterable<CmisObject> results = ctrl.getSession().queryObjects("cmis:document", "CONTAINS ('" + content + "')",
        false, oc);

    for (CmisObject cmisObject : results) {
      IDocument doc = new DocumentImpl((Document) cmisObject);
      docList.add(doc);
    }

    return docList;
  }

  /**
   * 
   * @param docList
   * @param content
   * @return
   */
  public List<String> queryFindLineContent(List<IResource> docList, String content) {

    List<String> peekContentList = new ArrayList<String>();

    for (IResource iResource : docList) {
      if (iResource instanceof DocumentImpl) {

        IDocument iDocument = (IDocument) iResource;

        try {
          Scanner scanner = new Scanner(new FileInputStream(iDocument.getDocumentPath(ctrl)));

          while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            // line, not scanner.
            if (line.contains(content)) // tag in the txt to locate position
            {
              peekContentList.add(line);
            }
          }
          scanner.close();

        } catch (Exception e) {
          System.out.println("File not found.");
        }
      }
    }
    return peekContentList;
  }

  public String queryFindLine(IResource resource, String content) {

    if (resource instanceof DocumentImpl) {

      IDocument iDocument = (IDocument) resource;

      try {
        scanner = new Scanner(ctrl.getDocumentContent(iDocument.getId()));

        while (scanner.hasNextLine()) {
          String line = scanner.nextLine().trim();

          if (line.contains(content)) {
            return line;
          }
        }
        scanner.close();

      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return null;
  }
}
