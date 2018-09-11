package com.oxygenxml.cmis.actions;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IFolder;
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
  private String versioningState;
  private CheckInDocInputPanel inputPanel;

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

    DocumentImpl doc = ((DocumentImpl) resource);
    if (doc.canUserCheckin()) {
      if (doc.isCheckedOut() && doc.isPrivateWorkingCopy()) {

        this.enabled = true;

      } else {
        this.enabled = false;
      }
    } else {
      this.enabled = false;
    }

    inputPanel = new CheckInDocInputPanel();
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
    String message = JOptionPane.showInputDialog(null, inputPanel, "New comment here");
    versioningState = inputPanel.getVersioningState();

    boolean majorCheckin = versioningState.equals("MAJOR") ? true : false;
    System.out.println("Version major?=" + majorCheckin);
    // The id received of the object after the check in
    ObjectId res = null;

    // Cast to the custom interface to use its methods
    DocumentImpl doc = ((DocumentImpl) resource);

    // Try to <Code>checkIn</Code>
    try {

      // Commit the <Code>checkIn</Code> and Get the ObjectId
      res = (ObjectId) doc.checkIn(majorCheckin, message);

      if (currentParent.getId().equals("#search.results")) {
        // currentParent.refresh();
        ((IFolder) currentParent).removeFromModel(resource);

        Document checkedInResource = CMISAccess.getInstance().createResourceController().getDocument(res.getId());
        ((IFolder) currentParent).addToModel(checkedInResource);
      } else {
        currentParent.refresh();
        itemsPresenter.presentResources(currentParent);
      }

    } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException ev) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }
  }
}

class CheckInDocInputPanel extends JPanel implements ActionListener {
  private JLabel versionLabel;
  private JRadioButton radioItemMajor;
  private JRadioButton radioItemMinor;
  private JPanel radioPanel;

  private String versioningState;

  public CheckInDocInputPanel() {
    radioPanel = new JPanel(new GridLayout(1, 2));
    versionLabel = new JLabel("Enter the message: ");

    setLayout(new GridLayout(1, 2, 0, 0));
    add(versionLabel);

    // MAJOR
    radioItemMajor = new JRadioButton("Major");
    radioItemMajor.setActionCommand("MAJOR");
    radioItemMajor.addActionListener(this);
    // Set selected
    radioItemMajor.setSelected(true);
    versioningState = "MAJOR";

    // MINOR
    radioItemMinor = new JRadioButton("Minor");
    radioItemMinor.setActionCommand("MINOR");
    radioItemMinor.addActionListener(this);

    radioPanel.add(radioItemMajor);
    radioPanel.add(radioItemMinor);

    add(radioPanel);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equals("MAJOR")) {
      radioItemMinor.setSelected(false);

      versioningState = e.getActionCommand();
    }

    if (e.getActionCommand().equals("MINOR")) {
      radioItemMajor.setSelected(false);

      versioningState = e.getActionCommand();
    }
  }

  public String getVersioningState() {
    return this.versioningState;
  }
}
