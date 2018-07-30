package com.oxygenxml.cmis.core.model;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.ObjectId;

import com.oxygenxml.cmis.core.ResourceController;

public interface IDocument extends IResource {
  
  String getDocumentPath(ResourceController ctrl);
  
  /*
   * Get the last version of the doc
   */
  Document getLastVersionDocument();
  
  /*
   * Check if the doc was checked-out
   */
  boolean isCheckedOut();
  
  /*
   * Check out the document
   */
  Document checkOut(DocumentType docType);
  
  /*
   * Cancel the check-out
   */
  void cancelCheckOut();
  
  /*
   * Check-in the document
   */
  ObjectId checkIn();
  
}
