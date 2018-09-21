package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

/**
 * Describes the check out action on a document by extending the AbstractAction
 * class
 * 
 * @author bluecc
 *
 */
public class CheckoutDocumentAction extends AbstractAction {
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(CheckoutDocumentAction.class);

  // The resource that will receive
  private transient IResource resource = null;
  private transient IResource currentParent = null;
  private transient ResourcesBrowser itemsPresenter = null;

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * @param itemsPresenter
   * @param currentParent
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CheckoutDocumentAction(IResource resource, IResource currentParent, ResourcesBrowser itemsPresenter) {
    super("Check out");

    // Set logger level

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
    DocumentImpl doc = ((DocumentImpl) resource);

    boolean isCheckedout = (doc.isCheckedOut() || doc.isPrivateWorkingCopy());
    setEnabled(doc.canUserCheckout() && !isCheckedout);
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the document
   * 
   * @param e
   * @exception org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    // The private copy of the document
    Document res = null;

    // Cast to the custom interface
    DocumentImpl doc = ((DocumentImpl) resource);

    // Try committing <Code>checkOut</Code>
    try {

      // Get the document
      res = doc.checkOut(doc.getDocType());

      if (currentParent.getId().equals("#search.results")) {
        ((IFolder) currentParent).addToModel(res);
        ((IFolder) currentParent).removeFromModel(resource);
      } else {
        currentParent.refresh();
        itemsPresenter.presentResources(currentParent);
      }

    } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException ev) {

      // SHow the exception if there is one
      logger.debug("Exception ", ev);
    }

  }
}
