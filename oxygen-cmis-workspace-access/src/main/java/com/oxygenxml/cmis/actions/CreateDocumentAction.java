package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.ui.CreateDocDialog;
import com.oxygenxml.cmis.ui.ResourcesBrowser;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes the copy action on a document by extending the AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CreateDocumentAction extends AbstractAction {
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(CreateDocumentAction.class);
  private static final ResourceController resourceController = CMISAccess.getInstance().createResourceController();
  // Parent of the resource
  private transient IResource currentParent;
  // Presenter to use to show the resources
  private final transient ResourcesBrowser itemsPresenter;
  private transient PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
  private transient String versioningState;
  private final  JFrame mainFrame = (JFrame) pluginWorkspace.getParentFrame();

  /**
   * Constructor that receives data to process for the creation and presentation
   * of the newly created document
   * 
   * @param resource
   * @param currentParent
   * @param itemsPresenter
   */
  public CreateDocumentAction(IResource currentParent, ResourcesBrowser itemsPresenter) {
    // Set a name and use a native icon
    super("Create document ", UIManager.getIcon("FileView.fileIcon"));

    // Set logger level
    logger.setLevel(Level.DEBUG);
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

    
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the document
   * 
   * @param e
   * @exception UnsupportedEncodingException
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    CreateDocDialog inputDialog;

    Document doc;
    Document docToOpen = null;
    Folder parentFolder;
    String mimeType;
    String fileName = null;

    int result = 0;

    do {
      // Create the input dialog
      inputDialog = new CreateDocDialog(mainFrame);
      fileName = inputDialog.getFileName();
      result = inputDialog.getResult();

      if (result == 0) {
        break;
      }

    } while (fileName == null);

    // Get versioning state
    versioningState = inputDialog.getVersioningState();

    if (result == 1 && !fileName.equals("")) {

      // Try creating the document with the string from the input
      try {
        parentFolder = ((FolderImpl) currentParent).getFolder();
        mimeType = MimeTypes.getMIMEType(fileName);

        // NON VERSIONABLE DOCUMENT
        if (versioningState.equals("NONE")) {
          logger.debug("None");
          doc = resourceController.createDocument(parentFolder, fileName, "", mimeType);
          docToOpen = doc;

        } else {
          docToOpen = createVersionableDoc(docToOpen, parentFolder, mimeType, fileName);
        }

      } catch (UnsupportedEncodingException e1) {

        // Show the exception if there is one
        JOptionPane.showMessageDialog(mainFrame, "Unsupported encoding: " + e1.getMessage());

      } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException e2) {
        // Show the exception if there is one
        JOptionPane.showMessageDialog(mainFrame, "Document already exists " + e2.getMessage());
      }

    }

    openInOxygenDoc(pluginWorkspace, docToOpen, mainFrame);

    currentParent.refresh();
    itemsPresenter.presentResources(currentParent.getId());
  }

  private void openInOxygenDoc(PluginWorkspace pluginWorkspace, Document docToOpen, JFrame mainFrame) {
    // -------- Open into Oxygen
    String urlAsTring = null;
    if (docToOpen != null) {
      // Get the <Code>getCustomURL</Code> of the document created
      urlAsTring = CmisURLConnection.generateURLObject(docToOpen, resourceController);

      // Check if it's not null
      if (pluginWorkspace != null) {

        // Try openning the url in the workspace
        try {
          pluginWorkspace.open(new URL(urlAsTring));

        } catch (MalformedURLException e1) {

          // Show the exception if there is one
          JOptionPane.showMessageDialog(mainFrame, "Exception " + e1.getMessage());
        }

      }
    }
    // --------
  }

  private Document createVersionableDoc(Document docToOpen, Folder parentFolder, String mimeType, String fileName)
      throws UnsupportedEncodingException {
    Document doc;

    logger.debug("Versionable");
    try {
      // Create a versioned document with the state of MAJOR
      doc = resourceController.createVersionedDocument(parentFolder, fileName, "", mimeType, "VersionableType",
          VersioningState.valueOf(versioningState));

      // Checkout the document

      DocumentImpl documentCreated = new DocumentImpl(doc);
      documentCreated.checkOut(documentCreated.getDocType());
      docToOpen = documentCreated.getDoc();

    } catch (Exception e2) {
      // Show the exception if there is one
      JOptionPane.showMessageDialog(mainFrame, "Exception " + e2.getMessage());
    }
    return docToOpen;
  }

}
