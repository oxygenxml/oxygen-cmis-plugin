package com.oxygenxml.cmis.ui;

import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;

/**
 * Make sure documents can be presenter from exterior
 * 
 * @author bluecc
 *
 */
public interface TabsPresenter {
  /**
   * Present a document
   * 
   * @param doc
   */
  void presentItem(Document doc);

}
