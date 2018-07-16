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
  private ArrayList<IDocument> docList;
  private ArrayList<IFolder> folderList;
  
  /**
   * CONSTRUCTOR
   * @param RESOURCE CONTROLER ctrl
   */
  public SearchController(ResourceController ctrl) {
    this.ctrl = ctrl;
    docList = new ArrayList<IDocument>();
    folderList = new ArrayList<IFolder>();
  }
  
  /**
   * SEARCH FUNCTION
   * @param name
   */
  public void searchFiles(String name) {
    OperationContext oc = ctrl.getSession().createOperationContext();
    oc.setFilterString("cmis:objectId,cmis:name,cmis:createdBy");
    
    queringDoc(name, oc);
    queringFolder(name, oc);
  }
  
  /**
   * HELPER FOR DOCUMENTS
   * @param cmisType
   * @param name
   * @param oc
   */
  private void queringDoc(String name, OperationContext oc) {
    ItemIterable<CmisObject> results = ctrl.getSession().queryObjects("cmis:document", "cmis:name LIKE '" + name + "'", false, oc);
    
    for(CmisObject cmisObject : results) {
      IDocument doc = new DocumentImpl((Document) cmisObject);
      docList.add(doc);
    }
  }
  
  /**
   * HELPER FOR FOLDERS
   * @param cmisType
   * @param name
   * @param oc
   */
  private void queringFolder(String name, OperationContext oc) {
  ItemIterable<CmisObject> results = ctrl.getSession().queryObjects("cmis:folder", "cmis:name LIKE '" + name + "'", false, oc);
  
  for(CmisObject cmisObject : results) {
     IFolder doc = new FolderImpl((Folder) cmisObject);
     folderList.add(doc);
    }
  }
  
  /**
   * RETURN IDocuments RESULTS
   * @return docList (ArrayList)
   */
  public ArrayList<IDocument> resultDocs(){
    return docList;
  }
  
  /**
   * RETURN IFolder RESULTS
   * @return folderList (ArrayList)
   */
  public ArrayList<IFolder> resultFolders(){
    return folderList;
  }
}
