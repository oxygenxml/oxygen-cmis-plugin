package com.oxygenxml.cmis.core.model.impl;

import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;

/**
 * CMIS document implementation. 
 */
public class DocumentImpl implements IDocument {
  /**
   * Wrapped CMIS document.
   */
  private Document doc;

  public DocumentImpl(Document doc) {
    this.doc = doc;
  }

  @Override
  public Iterator<IResource> iterator() {
    // The document is a leaf. 
    return Collections.emptyIterator();
  }
  
  /**
   * @return The wrapped document.
   */
  public Document getDoc() {
    return doc;
  }

  @Override
  public String getDisplayName() {
    return doc.getName();
  }

  @Override
  public String getId() {
    // TODO Auto-generated method stub
    return doc.getId();
  }

  
  public ItemIterable<QueryResult> getQuery(ResourceController ctrl) {
    String query = "SELECT * FROM cmis:document WHERE cmis:name LIKE '".concat(getDisplayName()).concat("'");
    return ctrl.getSession().query(query, false);
  }

}