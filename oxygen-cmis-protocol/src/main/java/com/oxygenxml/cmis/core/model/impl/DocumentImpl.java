package com.oxygenxml.cmis.core.model.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.commons.enums.Action;

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

  public String getContentStreamId() {
    return doc.getContentStreamId();
  }

  public String getVersionSeriesCheckedOutId() {
    return doc.getVersionSeriesCheckedOutId();
  }

  public DocumentType getDocType() {
    return doc.getDocumentType();
  }

  public String getModifiedBy() {
    String displayTime = "";
    Date timeModified = doc.getLastModificationDate().getTime();
    Date today = new Date();

    long diff = today.getTime() - timeModified.getTime();

    long diffSeconds = diff / 1000 % 60;
    long diffMinutes = diff / (60 * 1000) % 60;
    long diffHours = diff / (60 * 60 * 1000) % 24;
    long diffDays = diff / (24 * 60 * 60 * 1000);

    if (diffDays > 0) {
      displayTime = String.valueOf(diffDays) + " day/s ago";

    } else if (diffHours > 0) {
      displayTime = String.valueOf(diffHours) + " hour/s ago";

    } else if (diffMinutes > 0) {
      displayTime = String.valueOf(diffHours) + " minute/s ago";

    } else if (diffSeconds > 0) {
      displayTime = String.valueOf(diffSeconds) + " second/s ago";

    }

    return "Modified by " + doc.getLastModifiedBy() + " " + displayTime;
  }

  public boolean canUserDelete() {
    return doc.hasAllowableAction(Action.CAN_DELETE_OBJECT);

  }

  /**
   * Any use can get the content stream
   * 
   * @return
   */
  public boolean canUserOpen() {
    return doc.hasAllowableAction(Action.CAN_GET_CONTENT_STREAM);

  }

  public boolean canUserCreateDoc() {
    return doc.hasAllowableAction(Action.CAN_CREATE_DOCUMENT);
  }

  public boolean canUserUpdateContent() {
    return doc.hasAllowableAction(Action.CAN_SET_CONTENT_STREAM);
  }

  public boolean canUserUpdateProperties() {
    return doc.hasAllowableAction(Action.CAN_UPDATE_PROPERTIES);
  }

  /**
   * Checks if current user can check out
   * 
   * @return boolean
   */
  public boolean canUserCheckout() {
    return doc.hasAllowableAction(Action.CAN_CHECK_OUT);

  }

  /**
   * Checks if the current user can check in
   * 
   * @return boolean
   */
  public boolean canUserCheckin() {
    return doc.hasAllowableAction(Action.CAN_CHECK_IN);
  }

  /**
   * Checks if the current user can cancel the checkout
   * 
   * @return boolean
   */
  public boolean canUserCancelCheckout() {
    return doc.hasAllowableAction(Action.CAN_CANCEL_CHECK_OUT);

  }

  public boolean isVersionable() {
    return doc.isVersionable();
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
    // System.out.println("Doc path:" + docPath);

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
      System.out.println("PWC ID=" + pwcId);
      System.out.println("PWC name=" + pwc.getName());

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
  public ObjectId checkIn(boolean major, String message) {
    return doc.checkIn(major, null, doc.getContentStream(), message);
  }

  @Override
  public void refresh() {
    doc.refresh();
  }

  public CmisObject rename(String newName) {
    return doc.rename(newName);
  }
}