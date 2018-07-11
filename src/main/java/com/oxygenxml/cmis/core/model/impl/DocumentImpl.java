package com.oxygenxml.cmis.core.model.impl;

import java.util.Iterator;

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
    return null;
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
}
