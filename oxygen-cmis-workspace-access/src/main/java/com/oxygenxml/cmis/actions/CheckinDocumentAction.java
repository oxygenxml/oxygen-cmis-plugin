package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.ObjectId;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.ui.ItemListView;
import com.oxygenxml.cmis.ui.ItemsPresenter;

/**
 * Describes the check in action on a document by extending the AbstractAction
 * class
 * 
 * @author bluecc
 *
 */
public class CheckinDocumentAction extends AbstractAction {

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
  public CheckinDocumentAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {
    super("Check in");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
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

    // The id received of the object after the check in
    ObjectId res = null;

    // Cast to the custom interface to use its methods
    DocumentImpl doc = ((DocumentImpl) resource);

    // Try to <Code>checkIn</Code>
    try {

      // Commit the <Code>checkIn</Code> andgGet the ObjectId
      res = (ObjectId) doc.checkIn();

      currentParent.refresh();
      itemsPresenter.presentResources(currentParent);

    } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException ev) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }
}
