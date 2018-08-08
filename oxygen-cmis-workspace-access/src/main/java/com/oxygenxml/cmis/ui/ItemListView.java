package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.actions.CancelCheckoutDocumentAction;
import com.oxygenxml.cmis.actions.CancelCheckoutFolderAction;
import com.oxygenxml.cmis.actions.CheckinDocumentAction;
import com.oxygenxml.cmis.actions.CheckinFolderAction;
import com.oxygenxml.cmis.actions.CheckoutDocumentAction;
import com.oxygenxml.cmis.actions.CheckoutFolderAction;
import com.oxygenxml.cmis.actions.CopyDocumentAction;
import com.oxygenxml.cmis.actions.CopyFolderAction;
import com.oxygenxml.cmis.actions.CreateDocumentAction;
import com.oxygenxml.cmis.actions.CreateFolderAction;
import com.oxygenxml.cmis.actions.DeleteDocumentAction;
import com.oxygenxml.cmis.actions.DeleteFolderAction;
import com.oxygenxml.cmis.actions.OpenDocumentAction;
import com.oxygenxml.cmis.actions.PasteDocumentAction;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

/**
 * Describes how the folders and documents are: displayed, rendered, their
 * actions.
 * 
 * 
 * @author bluecc
 *
 */
public class ItemListView extends JPanel implements ItemsPresenter, ListSelectionListener {

  // All the resources recieved
  private JList<IResource> resourceList;
  // Popup menu foe each type of element (folder,document)
  private JPopupMenu menu;

  // Current folder inside
  private IResource currentParent;
  private DefaultListCellRenderer regularRenderer;

  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(ItemListView.class);

  /**
   * Constructor that gets the tabPresenter to show documents in tabs and
   * breacrumbPrsenter to update the breadcrumb
   * 
   * @param tabsPresenter
   * @param breadcrumbPresenter
   */
  ItemListView(TabsPresenter tabsPresenter, BreadcrumbPresenter breadcrumbPresenter) {

    // Create the listItem
    resourceList = new JList<IResource>();
    resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    resourceList.setSelectedIndex(0);
    resourceList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    resourceList.addListSelectionListener(this);

    // Scroller for the listRepo
    JScrollPane listItemScrollPane = new JScrollPane(resourceList);

    /*
     * Render all the elements of the listItem when necessary
     * 
     * @return Component
     * 
     * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
     * 
     * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
     */
    regularRenderer = new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
          boolean cellHasFocus) {

        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (component instanceof JLabel) {

          String renderText = "";

          if (value != null) {

            // Cast in order to use the methods from IResource interface
            renderText = ((IResource) value).getDisplayName();
            ((JLabel) component).setText(renderText);

            // If it's an instance of custom type of Folder
            if ((IResource) value instanceof FolderImpl) {

              // Set the native icon to the component
              ((JLabel) component).setIcon(UIManager.getIcon("FileView.directoryIcon"));

            } else if ((IResource) value instanceof DocumentImpl) {

              // If it's an instance of custom type of Folder
              // Set the native icon to the component
              ((JLabel) component).setIcon(UIManager.getIcon("FileView.fileIcon"));
            }

          }
        }

        return component;
      }
    };
    resourceList.setCellRenderer(regularRenderer);
    
    /*
     * Add listener to the entire list
     * 
     * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
     * 
     * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
     */
    resourceList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {

        // Get the location of the item using location of the click
        int itemIndex = resourceList.locationToIndex(e.getPoint());

        // Get the current item
        IResource currentItem = resourceList.getModel().getElementAt(itemIndex);

        // If right click was pressed
        if (SwingUtilities.isRightMouseButton(e)) {
          menu = new JPopupMenu();

          // Get the bounds of the item
          Rectangle cellBounds = resourceList.getCellBounds(itemIndex, itemIndex);

          // Check if the lick was outside the visible list
          if (!cellBounds.contains(e.getPoint())) {

            // Create de menu for the outside list
            createExternalListJMenu();

          } else {

            // Set selected on right click
            resourceList.setSelectedIndex(itemIndex);

            if (currentItem instanceof DocumentImpl) {

              // Create the JMenu for the document
              createDocumentJMenu(currentItem);

            } else if (currentItem instanceof FolderImpl) {

              // Create the JMenu for the folder
              createFolderJMenu(currentItem);

            }
          }

          // Bounds of the click
          menu.show(ItemListView.this, e.getX(), e.getY());

        }

        // Check if user clicked two times
        if (itemIndex != -1 && e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {

          // Check whether the item in the list
          if (itemIndex != -1) {
            System.out.println("TO present breadcrumb=" + currentItem.getDisplayName());

            if (!(currentItem instanceof DocumentImpl)) {
              // Present the next item (folder)
              breadcrumbPresenter.presentBreadcrumb(currentItem);
            }

            /*
             * If it's an document show it on a tab instead of iterating the
             * children
             */
            if (currentItem instanceof DocumentImpl) {

              // Present the document in tabs
              tabsPresenter.presentItem(((DocumentImpl) currentItem).getDoc());

              // Present document in Oxygen
              new OpenDocumentAction(currentItem).openDocumentPath();

            } else {

              // Present the folder children
              presentResources(currentItem);
            }

          }
        }
        ;

      }
    });

    // Set layout
    setLayout(new BorderLayout(0, 0));

    add(listItemScrollPane, BorderLayout.CENTER);

  }

  /*
   * JMenu for outside of the list components
   */
  private void createExternalListJMenu() {

    // Create a document in the current folder
    menu.add(new CreateDocumentAction(currentParent, currentParent, this));
    // Create a folder in the current folder
    menu.add(new CreateFolderAction(currentParent, this));
  }

  /*
   * JMenu for the Document
   */
  private void createDocumentJMenu(final IResource selectedResource) {

    // CRUD Document
    menu.add(new OpenDocumentAction(selectedResource));
    menu.add(new CopyDocumentAction(selectedResource));
    menu.add(new DeleteDocumentAction(selectedResource, currentParent, this));
    menu.add(new CheckinDocumentAction(selectedResource));
    menu.add(new CheckoutDocumentAction(selectedResource));
    menu.add(new CancelCheckoutDocumentAction(selectedResource));

    // TODO Check if is removed one reference or all maybe use of
    // removeFromFolder

    // TODO Drag and drop
    // Move to Folder
  }

  /*
   * JMenu for the Folder
   */

  private void createFolderJMenu(final IResource selectedResource) {

    // CRUD Folder

    menu.add(new CreateDocumentAction(selectedResource, currentParent, this));

    // TODO copy all resources postponed
    menu.add(new CopyFolderAction(selectedResource));

    menu.add(new PasteDocumentAction(selectedResource, currentParent, this));
    menu.add(new DeleteFolderAction(selectedResource, currentParent, this));

    menu.add(new CheckinFolderAction(selectedResource));
    menu.add(new CheckoutFolderAction(selectedResource));
    menu.add(new CancelCheckoutFolderAction(selectedResource));

  }

  /*
   * Implemented presentItems using connectionInfo and repoID !! Model shall be
   * created whenever the list is updated Facade Pattern
   */
  @Override
  public void presentItems(URL connectionInfo, String repositoryID) {
    try {
      // Get the instance
      CMISAccess instance = CMISAccess.getInstance();

      boolean connected = false;
      // Connect
      UserCredentials uc = null;
      do {
        try {

          // Try to connect to the repository
          instance.connectToRepo(connectionInfo, repositoryID, uc);

          // Get the rootFolder and set the model
          ResourceController resourceController = instance.createResourceController();

          Folder rootFolder = resourceController.getRootFolder();

          final FolderImpl origin = new FolderImpl(rootFolder);
          setFolder(origin);

          connected = true;

        } catch (CmisUnauthorizedException e) {

          // Get the credentials and show login dialog if necessary
          uc = AuthenticatorUtil.getUserCredentials(connectionInfo);
          System.out.println("User credit item list" + uc.getUsername());
        }

      } while (!connected);

    } catch (UserCanceledException e1) {
      logger.error(e1, e1);

      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
    }

  }

  /**
   * Set the root folder and use the model to be rendered
   * 
   * @param origin
   */
  private void setFolder(final FolderImpl origin) {
    DefaultListModel<IResource> model = new DefaultListModel<>();

    model.addElement(origin);
    resourceList.setModel(model);
  }

  /**
   * Presents all the children of the resource inside the list.
   * 
   * @param resource
   *          the resource to present its children.
   */
  private void presentResources(IResource parentResource) {
    
    installRenderer(parentResource.getId());
    
    this.currentParent = parentResource;

    System.out.println("Current item=" + parentResource.getDisplayName());
    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = parentResource.iterator();

    // Iterate them till it has a child
    if (childrenIterator != null) {

      // Define a model for the list in order to render the items
      DefaultListModel<IResource> model = new DefaultListModel<>();

      // While has a child, add to the model
      while (childrenIterator.hasNext()) {
        IResource iResource = (IResource) childrenIterator.next();
        model.addElement(iResource);

      }

      // Set the model to the list
      resourceList.setModel(model);
    }
  }

  @Override
  public void valueChanged(ListSelectionEvent e) {

  }

  @Override
  public void presentFolderItems(String folderID) {
    
    installRenderer(folderID);

    ResourceController resourceController = CMISAccess.getInstance().createResourceController();
    // Present the folder children
    presentResources(new FolderImpl(resourceController.getFolder(folderID)));
  }

  @Override
  public void presentFolderItems(IFolder folder) {
    
    installRenderer(folder.getId());

    presentResources(folder);
  }

  private void installRenderer(String id) {
    if ("#search.results".equals(id)) {
      /**
       * Testing version of the renderer
       */
      resourceList.setCellRenderer(new SearchResultCellRenderer());
    } else {
      resourceList.setCellRenderer(regularRenderer);
    }
  }

}
