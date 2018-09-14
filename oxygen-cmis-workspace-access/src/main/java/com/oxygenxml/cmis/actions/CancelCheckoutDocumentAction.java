package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.ui.ItemsPresenter;

/**
 * Describes the cancel checkout action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CancelCheckoutDocumentAction extends AbstractAction {

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
  public CancelCheckoutDocumentAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {
    super("Cancel check out");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

    DocumentImpl doc = ((DocumentImpl) resource);
    boolean canCancel = doc.canUserCancelCheckout() && doc.isCheckedOut() && doc.isPrivateWorkingCopy();
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
    final DocumentImpl doc = ((DocumentImpl) resource);

    // Try to do the cancel checkout
    try {

      // Commit the <Code>cancelCheckOut</Code>
      doc.cancelCheckOut();

      if (currentParent.getId().equals("#search.results")) {
        ((IFolder) currentParent).removeFromModel(resource);
        currentParent.refresh();

      } else {
        currentParent.refresh();
        itemsPresenter.presentResources(currentParent);
      }

    } catch (final org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException ev) {

      // Show the exception if there is one
      // TODO Cristian Dialogs need a parent to ensure the proper hierarchy. For example, if the parent is missing,
      // the dialog might end up on the wrong screen.
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }
}
