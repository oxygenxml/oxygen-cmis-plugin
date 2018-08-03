package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;

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

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CancelCheckoutDocumentAction(IResource resource) {
    super("Cancel check out");

    this.resource = resource;
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
    DocumentImpl doc = ((DocumentImpl) resource);

    // Try to do the cancel checkout
    try {

      //Commit the <Code>cancelCheckOut</Code>
      doc.cancelCheckOut();

    } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException ev) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }
}
