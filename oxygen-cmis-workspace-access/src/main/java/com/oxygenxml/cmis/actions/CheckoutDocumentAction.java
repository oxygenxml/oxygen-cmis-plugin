package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.Document;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.ui.ItemListView;
import com.oxygenxml.cmis.ui.ItemsPresenter;

/**
 * Describes the check out action on a document by extending the AbstractAction
 * class
 * 
 * @author bluecc
 *
 */
public class CheckoutDocumentAction extends AbstractAction {

  // The resource that will receive
  private IResource resource = null;
  private IResource currentParent = null;
  private ItemsPresenter itemsPresenter = null;

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * @param itemsPresenter
   * @param currentParent
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CheckoutDocumentAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {
    super("Check out");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
    
    if (((DocumentImpl) resource).isCheckedOut()) {

      this.enabled = false;

    } else {
      this.enabled = true;
    }
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
        currentParent.refresh();

      } else {
        currentParent.refresh();
        itemsPresenter.presentResources(currentParent);
      }

    } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException ev) {

      // SHow the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }

    System.out.println(res);
  }
}
