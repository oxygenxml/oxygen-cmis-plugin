package com.oxygenxml.cmis.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;

/**
 * Describes the copy action on a document by extending the AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CopyDocumentAction extends AbstractAction {

  // The resource that will receive
  private transient IResource resource = null;

  /**
   * Constructor that receives the resource to process
   * 
   * @param resource
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  public CopyDocumentAction(IResource resource) {
    super("Copy");

    this.resource = resource;

    setEnabled(((DocumentImpl) resource).canUserCheckout());
  }

  /**
   * Set the text from the system clipboard
   * 
   * @param writeMe
   */
  private void setSysClipboardText(String writeMe) {

    // Initialize the clipboard
    Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();

    // Type of interface for transferring text
    Transferable tText = new StringSelection(writeMe);

    // Set the new text
    clip.setContents(tText, null);

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

    try {
      // Cast to the custom interface of a Document
      DocumentImpl doc = ((DocumentImpl) resource);

      // Save the id of the document to the clipboard
      setSysClipboardText(doc.getId());

    } catch (Exception ev) {

      // SHow the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }

}