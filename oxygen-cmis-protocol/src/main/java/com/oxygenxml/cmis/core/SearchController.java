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

    // query("", SEARCH_IN_DOCUMENT | SEARCH_IN_FOLDER);
  }

  /**
   * HELPER FOR DOCUMENTS
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
   * HELPER FOR DOCUMENTS
   * 
   * @param cmisType
   * @param name
   * @param oc
   */
  public List<IResource> queringDoc(String name) {
    return queryResourceName(name, SEARCH_IN_DOCUMENT);
  }

  /**
   * METHOD TO SEARCH DOCUMENTS WITH SPECIFIC CONTENT!
   * 
   * @param content
   * @return
   */
  public List<IDocument> queringDocContent(String content) {
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
   * HELPER FOR FOLDERS
   * 
   * @param cmisType
   * @param name
   * @param oc
   */
  public List<IResource> queringFolder(String name) {
    return queryResourceName(name, SEARCH_IN_FOLDER);
  }

  /**
   * 
   * @param docList
   * @param content
   * @return
   */
  public List<String> queryFindLineContent(List<IResource> docList, String content) {
    // MaybeReader
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

          if (line.contains(content)) 
          {
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
