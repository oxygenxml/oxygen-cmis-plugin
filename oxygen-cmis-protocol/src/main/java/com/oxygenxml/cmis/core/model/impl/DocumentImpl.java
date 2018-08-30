package com.oxygenxml.cmis.core.model.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.QueryResult;

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
    return doc.getId();
  }

  public DocumentType getDocType() {
    return doc.getDocumentType();
  }

  public String getMimetype() {
    return doc.getContentStreamMimeType();
  }
  @Override
  public String getDescription() {
    return doc.getDescription();
  }

  public long getSize() {
    return doc.getContentStream().getLength();
  }

  public Date getTimeCreated() {
    return doc.getCreationDate().getTime();
  }

  public String getCreatedBy() {
    return doc.getCreatedBy();
  }

  public ItemIterable<QueryResult> getQuery(ResourceController ctrl) {
    String query = "SELECT * FROM cmis:document WHERE cmis:name LIKE '".concat(getDisplayName()).concat("'");
    return ctrl.getSession().query(query, false);
  }

  /**
   * TODO Make test with deep ierarchy!
   */
  public String getDocumentPath(ResourceController ctrl) {
    StringBuilder b = new StringBuilder();
    List<String> docPath = doc.getPaths();
    System.out.println("Doc path:" + docPath);

    b.append("/").append(ctrl.getRootFolder().getName());

    for (int i = 0; i < docPath.size(); i++) {
      if (docPath.isEmpty()) {
        break;
      }
      b.append(docPath.get(i)).append("/");
    }
    return b.toString();
  }

  /*
   * @return The last version of the document
   * 
   * @see
   * com.oxygenxml.cmis.core.model.IDocument#getLastVersionDocument(org.apache.
   * chemistry.opencmis.client.api.Document)
   */

  @Override
  public Document getLastVersionDocument() {
    Document latest = null;

    if (Boolean.TRUE.equals(doc.isLatestVersion())) {

      latest = doc;
    } else {

      latest = doc.getObjectOfLatestVersion(false);
    }
    return latest;
  }

  /*
   * @return The boolean value Can show by whom
   * 
   * @see
   * com.oxygenxml.cmis.core.model.IDocument#isCheckedOut(org.apache.chemistry.
   * opencmis.client.api.Document)
   */
  @Override
  public boolean isCheckedOut() {
    // A property needed to be set on creation of the document in order to get
    // this property
    // System.out.println("Checked out
    // ="+doc.getProperty("cmis:isVersionSeriesCheckedOut").getValuesAsString());
    return doc.isVersionSeriesCheckedOut();
  }

  /*
   * @return The boolean value if is a kind of cmis:document
   * 
   * @see
   * com.oxygenxml.cmis.core.model.IDocument#isCheckedOut(org.apache.chemistry.
   * opencmis.client.api.Document)
   */
  @Override
  public boolean isPrivateWorkingCopy() {
    // System.out.println("CPWC
    // ="+doc.getProperty("cmis:isPrivateWorkingCopy").getValuesAsString());
    return doc.isPrivateWorkingCopy();
  }

  /*
   * @return The private copy of the document
   * 
   * @see com.oxygenxml.cmis.core.model.IDocument#checkOut(org.apache.chemistry.
   * opencmis.client.api.Document)
   */
  @Override
  public Document checkOut(DocumentType docType) {
    if (Boolean.TRUE.equals(docType.isVersionable())) {

      ObjectId pwcId = doc.checkOut();

      Document pwc = (Document) CMISAccess.getInstance().getSession().getObject(pwcId);

      return pwc;
    }
    return doc;
  }

  /*
   * @return
   * 
   * @see
   * com.oxygenxml.cmis.core.model.IDocument#cancelCheckOut(org.apache.chemistry
   * .opencmis.client.api.Document)
   */
  @Override
  public void cancelCheckOut() throws org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException {
    doc.cancelCheckOut();
  }

  /*
   * @return ObjectId of the document
   * 
   * @see com.oxygenxml.cmis.core.model.IDocument#checkIn(org.apache.chemistry.
   * opencmis.client.api.Document)
   */
  @Override
  public ObjectId checkIn() {

    return doc.checkIn(true, null, doc.getContentStream(), "new version");
  }

  @Override
  public void refresh() {
    doc.refresh();
  }
}