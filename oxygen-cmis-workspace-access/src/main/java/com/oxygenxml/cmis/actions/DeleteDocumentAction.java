package com.oxygenxml.cmis.actions;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.ui.ItemsPresenter;

/**
 * Describes the delete document action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class DeleteDocumentAction extends AbstractAction {

  // The resource to be deleted
  private IResource resource;
  // Parent of that resource
  private IResource currentParent;
  // Presenter to be able to update the content of the parent
  private ItemsPresenter itemsPresenter;
  private DeleteDocInputPanel inputPanel;
  private String deleteType;

  /**
   * Constructor that gets the resource to be deleted , currentParent and the
   * presenter to be able to show the updated content of it
   * 
   * @param resource
   * @param currentParent
   * @param itemsPresenter
   */
  public DeleteDocumentAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {
    // Set a name
    super("Delete");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

    if (((DocumentImpl) resource).canUserDelete()) {
      this.enabled = true;
    } else {
      this.enabled = false;
    }

    inputPanel = new DeleteDocInputPanel();
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the document
   * 
   * <b>This action will delete one this version of the document</b>
   * 
   * @param e
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    // Cast to the custom type of Document
    DocumentImpl doc = ((DocumentImpl) resource);
    // Get a file name from user
    String fileName;
    
    
    do {
      fileName = JOptionPane.showInputDialog(null, inputPanel, "myfile.txt");
    } while (!fileName.trim().equals(doc.getDisplayName()));
    
    try {
      deleteType = inputPanel.getDeleteType();

      // Try to delete <Code>deleteOneVersionDocument</Code>
      if (deleteType.equals("SINGLE")) {

        // Commit the deletion
        CMISAccess.getInstance().createResourceController().deleteOneVersionDocument(doc.getDoc());

      } else if (deleteType.equals("ALL")) {

        // Try to delete <Code>deleteAllVersionsDocument</Code>
        // Commit the deletion
        CMISAccess.getInstance().createResourceController().deleteAllVersionsDocument(doc.getDoc());
      }

      // Present the new content of the parent resource
      if (currentParent.getId().equals("#search.results")) {
        currentParent.refresh();

      } else {
        currentParent.refresh();
        itemsPresenter.presentResources(currentParent);
      }

    } catch (Exception ev) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }
}

class DeleteDocInputPanel extends JPanel implements ActionListener {
  private JLabel messageLabel;
  private JRadioButton radioItemAll;
  private JRadioButton radioItemSingle;
  private JPanel radioPanel;

  private String deleteType;

  public DeleteDocInputPanel() {
    radioPanel = new JPanel(new GridLayout(1, 2));
    messageLabel = new JLabel("Enter the name of the doc: ");

    setLayout(new GridLayout(1, 2, 0, 0));
    add(messageLabel);

    // Delete all versions
    radioItemAll = new JRadioButton("All versions");
    radioItemAll.setActionCommand("ALL");
    radioItemAll.addActionListener(this);
    // Set selected
    radioItemAll.setSelected(true);
    deleteType = "ALL";

    // MINOR
    radioItemSingle = new JRadioButton("Single version");
    radioItemSingle.setActionCommand("SINGLE");
    radioItemSingle.addActionListener(this);

    radioPanel.add(radioItemAll);
    radioPanel.add(radioItemSingle);

    add(radioPanel);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equals("ALL")) {
      radioItemSingle.setSelected(false);

      deleteType = e.getActionCommand();
    }

    if (e.getActionCommand().equals("SINGLE")) {
      radioItemAll.setSelected(false);

      deleteType = e.getActionCommand();
    }
  }

  public String getDeleteType() {
    return this.deleteType;
  }
}
