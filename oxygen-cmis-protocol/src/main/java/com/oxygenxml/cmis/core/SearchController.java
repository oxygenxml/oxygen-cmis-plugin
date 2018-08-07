package com.oxygenxml.cmis.core;

import java.util.ArrayList;
import java.util.List;

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

  /**
   * CONSTRUCTOR
   * @param RESOURCE CONTROLER ctrl
   */
  public SearchController(ResourceController ctrl) {
    this.ctrl = ctrl;
    
//    query("", SEARCH_IN_DOCUMENT | SEARCH_IN_FOLDER);
  }
  
  
  /**
   * HELPER FOR DOCUMENTS
   * @param cmisType
   * @param name
   * @param oc
   */
  private List<IResource> queryResourceName(String name, int searchObjectTypes) {
    ArrayList<IResource> resources = new ArrayList<>();
    
    OperationContext oc = ctrl.getSession().createOperationContext();
    oc.setFilterString("cmis:objectId,cmis:name,cmis:createdBy");
    String scope = "";
    if ((searchObjectTypes & SEARCH_IN_DOCUMENT) != 0) {
      scope = "cmis:document";
    }
    
    if ((searchObjectTypes & SEARCH_IN_FOLDER) != 0) {
      if (scope.length() > 0) {
        scope += ",";
      }
      scope += " cmis:folder";
    }
    
    ItemIterable<CmisObject> results = ctrl.getSession().queryObjects(scope, "cmis:name LIKE '%" + name + "%'", false, oc);
    
    for(CmisObject cmisObject : results) {
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
   * @param cmisType
   * @param name
   * @param oc
   */
  public List<IResource> queringDoc(String name) {
    return queryResourceName(name, SEARCH_IN_DOCUMENT);
  }
  
  /**
   * METHOD TO SEARCH DOCUMENTS WITH SPECIFIC CONTENT!
   * @param content
   * @return
   */
  public List<IDocument> queringDocContent(String content){
   ArrayList<IDocument> docList = new ArrayList<IDocument>();
    
    OperationContext oc = ctrl.getSession().createOperationContext();
    oc.setFilterString("cmis:objectId,cmis:name,cmis:createdBy");
    ItemIterable<CmisObject> results = ctrl.getSession().queryObjects("cmis:document", "CONTAINS ('" + content + "')", false, oc);
    
    for(CmisObject cmisObject : results) {
      IDocument doc = new DocumentImpl((Document) cmisObject);
      docList.add(doc);
    }
    
    return docList;
  }
  
  /**
   * HELPER FOR FOLDERS
   * @param cmisType
   * @param name
   * @param oc
   */
  public List<IResource> queringFolder(String name) {
    return queryResourceName(name, SEARCH_IN_DOCUMENT);
  }

}
