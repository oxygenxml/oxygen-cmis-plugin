package com.oxygenxml.cmis.core;

import java.io.FileInputStream;
import java.io.Reader;
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

  private ArrayList<IResource> removeBlockedDocFromSearch(ArrayList<IResource> resources) {
    for (int index = 0; index < resources.size(); index++) {
      if (resources.get(index).isCheckedOut() && !((DocumentImpl) resources.get(index)).isPrivateWorkingCopy()) {
        resources.remove(index);
      }
    }
    return resources;
  }

  /**
   * Find all the resources and order by name depending on what to search a
   * cmis:document or folder
   * 
   * @param content
   * @param searchObjectTypes
   * @return
   */
  private List<IResource> queryResource(String content, int searchObjectTypes) {
    ArrayList<IResource> resources = new ArrayList<>();

    OperationContext oc = ctrl.getSession().createOperationContext();
    oc.setOrderBy("cmis:name ASC");
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

    String where = "cmis:name LIKE '%" + content + "%' OR cmis:description LIKE '%" + content
        + "%' OR cmis:createdBy LIKE '%" + content + "%'" + "OR CONTAINS ('" + content + "')";

    ItemIterable<CmisObject> results = ctrl.getSession().queryObjects(scope, where, false, oc);

    for (CmisObject cmisObject : results) {
      IResource res = null;
      if (cmisObject instanceof Document) {
        res = new DocumentImpl((Document) cmisObject);
      } else {
        res = new FolderImpl((Folder) cmisObject);
      }

      resources.add(res);
    }
    resources = removeBlockedDocFromSearch(resources);
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

  // public String queryFindLine(IResource resource, String content) {
  //
  // final int STRING_LIMIT = 45;
  // if (resource instanceof DocumentImpl) {
  //
  // IDocument iDocument = (IDocument) resource;
  //
  // try {
  // scanner = new Scanner(ctrl.getDocumentContent(iDocument.getId()));
  //
  // while (scanner.hasNextLine()) {
  // String line = scanner.nextLine().trim();
  // System.out.println("Key="+content);
  // if (line.contains(content)) {
  //
  // System.out.println("Content found=" + line);
  // return limitStringResult(line, content, STRING_LIMIT);
  //
  // }
  // }
  // scanner.close();
  //
  // } catch (Exception e) {
  // e.printStackTrace();
  // }
  // }
  //
  // return null;
  // }
  public String queryFindLine(IResource resource, String content) {
    String[] searchKeys = content.trim().split("\\s+");
    final int STRING_LIMIT = 45;
    if (resource instanceof DocumentImpl) {

      IDocument iDocument = (IDocument) resource;

      try {
        Reader documentContent = ctrl.getDocumentContent(iDocument.getId());
        if (documentContent != null) {
          scanner = new Scanner(documentContent);

          while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            for (String key : searchKeys) {

              // System.out.println("Key context =" + key);
              if (line.contains(key)) {

                // System.out.println("Content found=" + line);
                return limitStringResult(line, key, STRING_LIMIT);

              }
            }

          }
          scanner.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  public String limitStringResult(String input, String pattern, int stringLimit) {
    String limitedString = input.trim();
    int frontCounter = 1;
    int backCounter = input.length() - 1;

//    int frontLimit = input.lastIndexOf(pattern);
//    int backLimit = input.lastIndexOf(pattern) + pattern.length() - 1;
    // System.out.println("String length =" + input.length());
    // System.out.println("Front limit =" + frontLimit);
    // System.out.println("Back limit =" + backLimit);

    while (limitedString.length() > stringLimit) {

      if (frontCounter != limitedString.indexOf(pattern)) {
        // System.out.println("front counter=" + frontCounter);
        // System.out.println("back counter=" + backCounter);
        limitedString = limitedString.substring(frontCounter);
        // System.out.println("The string after front cut = " + limitedString);
        // System.out.println("String length =" + limitedString.length());
        backCounter--;

      }

      if (limitedString.length() <= stringLimit) {
        return limitedString;
      }

      if (backCounter != limitedString.indexOf(pattern) + pattern.length() - 1) {
        // System.out.println("\nfront counter=" + frontCounter);
        // System.out.println("back counter=" + backCounter);
        limitedString = limitedString.substring(0, backCounter);
        // System.out.println("The string after back cut = " + limitedString);
        // System.out.println("String length =" + limitedString.length());
        backCounter--;

      }

      if (limitedString.length() <= stringLimit) {
        return limitedString;
      }

    }

    return limitedString;
  }
}
