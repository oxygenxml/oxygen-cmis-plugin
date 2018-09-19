package com.oxygenxml.cmis.core;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;

import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

public class SearchController {

  public static final int SEARCH_IN_DOCUMENT = 1;
  public static final int SEARCH_IN_FOLDER = 2;

  /**
   * Logic operators for the search that are supposed to be uppercase
   * 
   * @author bluecc
   *
   */
  public enum LogicOperators {
    AND, OR, NOT;
    public static boolean contains(String s) {
      for (LogicOperators choice : values())
        if (choice.name().equals(s))
          return true;
      return false;
    }
  };

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
   * Returns the resources searched by name either is a document or a folder
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
   * Removes the blocked documents from search
   * 
   * @param resources
   * @return
   */
  private ArrayList<IResource> removePWCDocsFromSearch(ArrayList<IResource> resources) {
    Iterator<IResource> resIterator = resources.iterator();

    while (resIterator.hasNext()) {

      IResource resource = resIterator.next();
      if (resource.isCheckedOut() && ((DocumentImpl) resource).isPrivateWorkingCopy()) {
        resIterator.remove();
      }
    }
    return resources;

  }

  /**
   * Find all the resources and order by name ascending depending on what to
   * search (ALL KEYS) a cmis:document or folder
   * 
   * @param content
   * @param searchObjectTypes
   * @return
   */
  private List<IResource> queryResource(String toSearch, int searchObjectTypes) {
    String[] searchKeys = toSearch.split("\\s+");

    ArrayList<IResource> resources = new ArrayList<>();

    OperationContext oc = ctrl.getSession().createOperationContext();
    oc.setOrderBy("cmis:name ASC");
    oc.setIncludeAllowableActions(true);
    oc.setIncludeRelationships(IncludeRelationships.BOTH);
    oc.setIncludePolicies(true);
    String scope = "";

    // Binary trick
    if ((searchObjectTypes & SEARCH_IN_DOCUMENT) != 0) {
      scope = "cmis:document";
    }

    else if ((searchObjectTypes & SEARCH_IN_FOLDER) != 0) {
      if (scope.length() > 0) {
        scope += ",";
      }
      scope += "cmis:folder";
    }

    // It's necessary to exist only one CONTAINS
    StringBuilder strBuild = new StringBuilder();
    strBuild.append("CONTAINS('");

    String searchKey = "";
    String logicKey = "";
    String key = "";

    for (int index = 0; index < searchKeys.length; index++) {
      // Get the current key
      key = searchKeys[index];

      // Check if it's an operator or not
      if (LogicOperators.contains(key)) {
        logicKey = key;
        // Append logic operator
        strBuild.append(logicKey);

      } else {
        searchKey = key;
        strBuild.append("(cmis:name:" + searchKey + " OR cmis:description:" + searchKey + " OR " + searchKey + ")");

        // Check if the is a next key (The case when there is only a space
        // between search keys.
        if (index + 1 < searchKeys.length) {
          key = searchKeys[index + 1];

          // Check if it's a logic key
          if (!LogicOperators.contains(key)) {
            logicKey = "AND";
            strBuild.append(logicKey);
          }
        }

      }

    }
    strBuild.append("')");

    String where = strBuild.toString();
    System.out.println("Where statement : " + where);
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
    return removePWCDocsFromSearch(resources);

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
   * Finds the line where the keys where found and return it The limit is 45
   * 
   * @param resource
   * @param content
   * @return string the limited string
   */

  public String queryFindLine(IResource resource, String content) {
    String[] searchKeys = content.trim().split("\\s+");
    final int STRING_LIMIT = 45;

    // Check if it's a document
    if (resource instanceof DocumentImpl) {

      IDocument iDocument = (IDocument) resource;
      if (iDocument != null) {
        try {
          // Use a reader for the content
          Reader documentContent = ctrl.getDocumentContent(iDocument.getId());

          // If there is something
          if (documentContent != null) {
            scanner = new Scanner(documentContent);

            // Iterare line by line
            while (scanner.hasNextLine()) {
              String line = scanner.nextLine().trim();

              // Check for each key
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
    }

    return null;
  }

  /**
   * Cuts the input until reaches the maximum limit of the string specified and
   * makes sure not to cut the pattern. The pattern consists only of one keyword
   * 
   * @param input
   * @param pattern
   * @param stringLimit
   * @return string the limited string
   */
  public String limitStringResult(String input, String pattern, int stringLimit) {
    String limitedString = input.trim();
    int frontCounter = 1;
    int backCounter = input.length() - 1;

    // While the input is larger than limit
    while (limitedString.length() > stringLimit) {

      // Not reached the front of the input
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

      // Not reached the back of input
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
