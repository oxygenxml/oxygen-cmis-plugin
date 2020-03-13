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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

public class SearchController {
  /**
   * Logging.
   */
  private static final Logger logger = LogManager.getLogger(SearchController.class);
  private static final String FOLDER_TYPE = "cmis:folder";
  private static final String DOCUMENT_TYPE = "cmis:document";
  public static final int SEARCH_IN_DOCUMENT = 1;
  public static final int SEARCH_IN_FOLDER = 2;

  /**
   * Logic operators for the search that are supposed to be upper case
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
  }

  private ResourceController ctrl;

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

    String scope = "";

    if ((searchObjectTypes & SEARCH_IN_DOCUMENT) != 0) {
      scope = DOCUMENT_TYPE;
    }

    else if ((searchObjectTypes & SEARCH_IN_FOLDER) != 0) {
      if (scope.length() > 0) {
        scope += ",";
      }
      scope += FOLDER_TYPE;
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
   * Find all the foreign resources by getting the personal documents and then
   * eliminate the other documents from all resources with given search keys.
   * 
   * @param content
   * @param searchObjectTypes
   * @return
   */
  private List<IResource> queryPersonalCheckedoutDocs(String toSearch, int searchObjectTypes) {
    List<DocumentImpl> personalDocuments = new ArrayList<>();
    List<String> idResources = new ArrayList<>();
    List<IResource> generalResults = new ArrayList<>();

    OperationContext oc = ctrl.getSession().createOperationContext();
    oc.setOrderBy("cmis:name ASC");
    oc.setIncludeAllowableActions(true);
    oc.setIncludeRelationships(IncludeRelationships.BOTH);
    oc.setIncludePolicies(true);

    // Get the personal checked out documents
    ItemIterable<Document> personalResults = ctrl.getSession().getCheckedOutDocs(oc);
    // Check what kind of results we've got.
    for (CmisObject cmisObject : personalResults) {
      IResource res = null;

      if (cmisObject instanceof Document) {
        res = new DocumentImpl((Document) cmisObject);

      } else if (cmisObject instanceof Folder) {
        res = new FolderImpl((Folder) cmisObject);
      }

      if (res != null) {
        // Get the original versions
        Document lastCheckoutVersion = ((DocumentImpl) res).getLastCheckoutVersion();
        personalDocuments.add(new DocumentImpl(lastCheckoutVersion));
      }
    }

    // Store the ids of the personal docs
    for (DocumentImpl document : personalDocuments) {
      idResources.add(document.getId());
    }

    // Get all the results with given seach keys
    generalResults.addAll(queryDoc(toSearch));

    // Eliminate those that are not personal
    Iterator<IResource> resultsIterator = generalResults.iterator();
    while (resultsIterator.hasNext()) {
      DocumentImpl doc = (DocumentImpl) resultsIterator.next();

      String docId = doc.getId();

      if (!idResources.contains(docId)) {
        resultsIterator.remove();
      }
    }

    return generalResults;

  }

  /**
   * Find all the foreign resources by getting the personal documents and then
   * eliminate the other documents from all resources with given search keys.
   * 
   * @param content
   * @return
   */
  public List<IResource> queryForeignCheckedoutDocs(String toSearch) {
    List<IResource> personalCheckedoutDocs = new ArrayList<>();
    List<IResource> allResources = new ArrayList<>();
    List<String> idResources = new ArrayList<>();

    // Get all personal documents
    personalCheckedoutDocs.addAll(queryPersonalCheckedout(toSearch));
    // Store the ids
    for (IResource resource : personalCheckedoutDocs) {

      if (resource instanceof DocumentImpl) {
        String docId = ((DocumentImpl) resource).getId();
        idResources.add(docId);

      }
    }

    // Get all the resources with given search keys
    allResources.addAll(queryDoc(toSearch));
    // Remove non checkout docs
    removeNonCheckoutDocuments(allResources);

    Iterator<IResource> allResourcesIterator = allResources.iterator();
    // Eliminate all personal documents
    while (allResourcesIterator.hasNext()) {

      IResource resource = allResourcesIterator.next();

      if (idResources.contains(resource.getId())) {
        allResourcesIterator.remove();
      }
    }

    return allResources;

  }

  /**
   * Removes the non checkout documents
   * 
   * @param resources
   * @return
   */
  private List<IResource> removeNonCheckoutDocuments(List<IResource> resources) {
    Iterator<IResource> resIterator = resources.iterator();

    while (resIterator.hasNext()) {

      IResource resource = resIterator.next();
      if (!resource.isCheckedOut()) {
        resIterator.remove();
      }
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
   * search (ALL KEYS) a cmis:document or folder.
   * 
   * @param content
   * @param searchObjectTypes
   * @return
   */
  private List<IResource> queryResource(String toSearch, int searchObjectTypes) {
    ArrayList<IResource> resources = new ArrayList<>();
    String[] searchKeys = toSearch.split("\\s+");
    String scope = "";

    OperationContext oc = ctrl.getSession().createOperationContext();
    oc.setOrderBy("cmis:name ASC");
    oc.setIncludeAllowableActions(true);
    oc.setIncludeRelationships(IncludeRelationships.BOTH);
    oc.setIncludePolicies(true);

    // Binary trick
    if ((searchObjectTypes & SEARCH_IN_DOCUMENT) != 0) {
      scope = DOCUMENT_TYPE;
    }

    else if ((searchObjectTypes & SEARCH_IN_FOLDER) != 0) {
      if (scope.length() > 0) {
        scope += ",";
      }
      scope += FOLDER_TYPE;
    }

    StringBuilder strBuild = constructWhereStatement(searchKeys);

    String where = strBuild.toString();

    if (logger.isDebugEnabled()) {
      logger.debug("Where statement : " + where);
    }

    // The results after the search.
    ItemIterable<CmisObject> results = ctrl.getSession().queryObjects(scope, where, false, oc);

    // Check what kind of results we've got.
    for (CmisObject cmisObject : results) {
      IResource res = null;

      if (cmisObject instanceof Document) {
        res = new DocumentImpl((Document) cmisObject);

      } else if (cmisObject instanceof Folder) {
        res = new FolderImpl((Folder) cmisObject);
      }

      resources.add(res);
    }
    return removePWCDocsFromSearch(resources);

  }

  /**
   * Constructs the statement using ONLY one CONTAINS because only this way all
   * the fields can be searched (case sensitive).
   * 
   * @param searchKeys
   * @return Final string to b e used for there where parameter in queryObjects
   *         method.
   */
  private StringBuilder constructWhereStatement(String[] searchKeys) {
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
        // When there is only one key search for everything that contains that
        // key
        if (searchKeys.length == 1) {
          searchKey = "*" + key + "*";

        } else {
          searchKey = key;
        }
        strBuild.append("(cmis:name:" + searchKey + " OR cmis:createdBy:" + searchKey + " OR cmis:description:"
            + searchKey + " OR " + searchKey + ")");

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
    return strBuild;
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

  public List<IResource> queryPersonalCheckedout(String content) {
    return queryPersonalCheckedoutDocs(content, SEARCH_IN_DOCUMENT);
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
   * Searches for specific content
   * 
   * @param content
   * @return
   */
  public List<IDocument> queryDocContent(String content) {
    ArrayList<IDocument> docList = new ArrayList<>();

    OperationContext oc = ctrl.getSession().createOperationContext();

    ItemIterable<CmisObject> results = ctrl.getSession().queryObjects(DOCUMENT_TYPE, "CONTAINS ('" + content + "')",
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

      try {
        // Use a reader for the content
        Reader documentContent = ctrl.getDocumentContent(iDocument.getId());

        // If there is something
        if (documentContent != null) {
          return scanLines(searchKeys, STRING_LIMIT, documentContent);
        }
      } catch (Exception e) {
        logger.debug("Exception", e);
      }

    }

    return null;
  }

  private String scanLines(String[] searchKeys, final int STRING_LIMIT, Reader documentContent) {
    Scanner scanner;
    scanner = new Scanner(documentContent);

    // Iterate line by line
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();

      // Check for each key
      for (String key : searchKeys) {

        // If line contains the key
        if (line.contains(key)) {

          logger.debug("Content found=" + line);
          scanner.close();
          return limitStringResult(line, key, STRING_LIMIT);
        }
      }

    }
    scanner.close();
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

        limitedString = limitedString.substring(frontCounter);
        // After Front cut

        backCounter--;
      }

      if (limitedString.length() <= stringLimit) {
        return limitedString;
      }

      // Not reached the back of input
      if (backCounter != limitedString.indexOf(pattern) + pattern.length() - 1) {

        limitedString = limitedString.substring(0, backCounter);
        // Back cut
        backCounter--;

      }

      if (limitedString.length() <= stringLimit) {
        return limitedString;
      }

    }

    return limitedString;
  }
}
