package com.oxygenxml.cmis.core.model.impl;

import java.util.Collections;
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
    // TODO Auto-generated method stub
    return doc.getId();
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

    b.append("/").append(ctrl.getRootFolder().getName());

    for (int i = 0; i < docPath.size(); i++) {
      if (docPath.isEmpty()) {
        break;
      }
      b.append(docPath.get(i)).append("/");
    }

    return b.toString();
  }
<<<<<<< HEAD
=======

  /*
   * @return The last version of the document
   * 
   * @see
   * com.oxygenxml.cmis.core.model.IDocument#getLastVersionDocument(org.apache.
   * chemistry.opencmis.client.api.Document)
   */

  @Override
  public Document getLastVersionDocument(Document doc) {
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
  public boolean isCheckedOut(Document doc) {

    boolean isCheckedOut = Boolean.TRUE.equals(doc.isVersionSeriesCheckedOut());
    String checkedOutBy = doc.getVersionSeriesCheckedOutBy();

    System.out.println(isCheckedOut + "checkout by" + checkedOutBy);
    return isCheckedOut;
  }

  /*
   * @return The private copy of the document
   * 
   * @see com.oxygenxml.cmis.core.model.IDocument#checkOut(org.apache.chemistry.
   * opencmis.client.api.Document)
   */
  @Override
  public Document checkOut(Document doc, DocumentType docType) {
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
  public void cancelCheckOut(Document doc) {
    // TODO Auto-generated method stub
    try {

      doc.cancelCheckOut();

    } catch (Exception e) {

      e.getMessage();
    }

  }

  /*
   * @return ObjectId of the document
   * 
   * @see com.oxygenxml.cmis.core.model.IDocument#checkIn(org.apache.chemistry.
   * opencmis.client.api.Document)
   */
  @Override
  public ObjectId checkIn(Document doc) {

    return doc.checkIn(true, null, doc.getContentStream(), "new version");
  }

<<<<<<< HEAD
>>>>>>> 3ec8e7e0cf58914289385f708b315d118223d494
=======
>>>>>>> 3ec8e7e0cf58914289385f708b315d118223d494
}