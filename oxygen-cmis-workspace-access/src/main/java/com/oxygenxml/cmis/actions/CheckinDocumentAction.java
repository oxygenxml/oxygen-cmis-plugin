package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.ObjectId;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;

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

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CheckinDocumentAction(IResource resource) {
    super("Check in");

    this.resource = resource;
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

    } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException ev) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }
}
