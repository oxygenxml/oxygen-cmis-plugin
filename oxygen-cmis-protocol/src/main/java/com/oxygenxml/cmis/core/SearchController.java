package com.oxygenxml.cmis.core;

import java.util.ArrayList;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

public class SearchController {
  
  private ResourceController ctrl;

  /**
   * CONSTRUCTOR
   * @param RESOURCE CONTROLER ctrl
   */
  public SearchController(ResourceController ctrl) {
    this.ctrl = ctrl;
  }
  
  
  /**
   * HELPER FOR DOCUMENTS
   * @param cmisType
   * @param name
   * @param oc
   */
  public ArrayList<IDocument> queringDoc(String name) {
    ArrayList<IDocument> docList = new ArrayList<IDocument>();
    
    OperationContext oc = ctrl.getSession().createOperationContext();
    oc.setFilterString("cmis:objectId,cmis:name,cmis:createdBy");
    ItemIterable<CmisObject> results = ctrl.getSession().queryObjects("cmis:document", "cmis:name LIKE '%" + name + "%'", false, oc);
    
    for(CmisObject cmisObject : results) {
      IDocument doc = new DocumentImpl((Document) cmisObject);
      docList.add(doc);
    }
    
    return docList;
  }
  
  /**
   * METHOD TO SEARCH DOCUMENTS WITH SPECIFIC CONTENT!
   * @param content
   * @return
   */
  public ArrayList<IDocument> queringDocContent(String content){
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
  public ArrayList<IFolder> queringFolder(String name) {
    ArrayList<IFolder> folderList = new ArrayList<IFolder>();
    
    OperationContext oc = ctrl.getSession().createOperationContext();
    oc.setFilterString("cmis:objectId,cmis:name,cmis:createdBy");
    ItemIterable<CmisObject> results = ctrl.getSession().queryObjects("cmis:folder", "cmis:name LIKE '%" + name + "%'", false, oc);
  
    for(CmisObject cmisObject : results) {
     IFolder fold = new FolderImpl((Folder) cmisObject);
     folderList.add(fold);
    }
    
    return folderList;
  }

}
