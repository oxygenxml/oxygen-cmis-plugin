package com.oxygenxml.cmis.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.CmisAccessSingleton;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.plugin.TranslationResourceController;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

/**
 * Describes the cancel checkout action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CancelCheckoutDocumentAction extends AbstractAction {
  // Internal role
  private static final String SEARCH_RESULTS_ID = "#search.results";
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(CancelCheckoutFolderAction.class);
  private final transient ResourceController resourceController;
  // The resource that will receive
  private transient IResource resource = null;
  private transient IResource currentParent = null;
  private transient ResourcesBrowser itemsPresenter = null;
  private transient DocumentImpl pwcDoc;

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * @param itemsPresenter
   * @param currentParent
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CancelCheckoutDocumentAction(IResource resource, IResource currentParent, ResourcesBrowser itemsPresenter) {
    super(TranslationResourceController.getMessage("CMIS_CANCEL_CHECK_OUT"));

    // Set logger level

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
    this.resourceController = CmisAccessSingleton.getInstance().createResourceController();

    DocumentImpl doc = ((DocumentImpl) resource);
    String pwcId = null;
    boolean hasPwc = false;
    boolean canUserCancelCheckout = false;

    // Check if the doc is checked-out and get the PWC id
    if (doc.isCheckedOut() && !doc.isPrivateWorkingCopy()) {
      pwcId = doc.getVersionSeriesCheckedOutId();
    }

    // If has a PWC id
    if (pwcId != null) {
      hasPwc = true;

      // Get the pwc
      Document pwc = (Document) resourceController.getSession().getObject(pwcId);
      pwcDoc = new DocumentImpl(pwc);

      // Allow cancelCheckout
      canUserCancelCheckout = pwcDoc.canUserCancelCheckout();
    }

    boolean canCancel = canUserCancelCheckout && doc.isCheckedOut() && hasPwc;
    setEnabled(canCancel);
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the document
   * 
   * @param e
   * @exception org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    // Try to do the cancel checkout
    try {

      // Commit the <Code>cancelCheckOut</Code>
      pwcDoc.cancelCheckOut();

      if (currentParent.getId().equals(SEARCH_RESULTS_ID)) {
        ((IFolder) currentParent).removeFromModel(resource);
        currentParent.refresh();

      } else {
        currentParent.refresh();
        itemsPresenter.presentResources(currentParent);
      }

    } catch (final org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException ev) {

      // Show the exception if there is one
      logger.error("Exception action ", ev);
    }
  }
}
