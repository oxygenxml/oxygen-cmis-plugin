package com.oxygenxml.cmis.core.model.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;

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

}
