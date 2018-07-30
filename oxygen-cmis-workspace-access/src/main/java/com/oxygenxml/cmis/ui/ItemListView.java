package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Iterator;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.chemistry.opencmis.client.api.Folder;

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
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

public class ItemListView extends JPanel implements ItemsPresenter, ListSelectionListener {

  private JList<IResource> resourceList;
  private JPopupMenu menu;
  private TabsPresenter tabsPresenter;
  private BreadcrumbPresenter breadcrumbPresenter;

  private IResource currentParent;

  ItemListView(TabsPresenter tabsPresenter, BreadcrumbPresenter breadcrumbPresenter) {

    this.tabsPresenter = tabsPresenter;
    this.breadcrumbPresenter = breadcrumbPresenter;

    // Create the listItem
    resourceList = new JList<IResource>();
    resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    resourceList.setSelectedIndex(0);
    resourceList.addListSelectionListener(this);

    // Scroller for the listRepo
    JScrollPane listItemScrollPane = new JScrollPane(resourceList);

    /*
     * Render all the elements of the listItem when necessary
     */
    resourceList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
          boolean cellHasFocus) {
        String renderTex = "";

        if (value != null) {
          // Cast in order to use the methods from IResource interface

          renderTex = ((IResource) value).getDisplayName();

        }
        return super.getListCellRendererComponent(list, renderTex, index, isSelected, cellHasFocus);
      }
    });

    /*
     * Add listener to the entire list
     */
    resourceList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        // Get the location of the item using location of the click
        int itemIndex = resourceList.locationToIndex(e.getPoint());
        IResource currentItem = resourceList.getModel().getElementAt(itemIndex);

        if (SwingUtilities.isRightMouseButton(e)) {
          menu = new JPopupMenu();
          
          System.out.println("Current index=" + itemIndex);

          Rectangle cellBounds = resourceList.getCellBounds(itemIndex, itemIndex);

          System.out.println(cellBounds);
          System.out.println(e.getPoint());
          
          
          // Check if the lick was outside the visible list
          if (!cellBounds.contains(e.getPoint())) {
            
            createExternalListJMenu();
            
          } else {

            // Set selected on right click
            resourceList.setSelectedIndex(itemIndex);

            if (currentItem instanceof DocumentImpl) {

              createDocumentJMenu(currentItem);

            } else if (currentItem instanceof FolderImpl) {

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
              breadcrumbPresenter.presentBreadcrumb(currentItem);
            }

            /*
             * If it's an document show it on a tab instead of iterating the
             * children
             */
            if (currentItem instanceof DocumentImpl) {
              tabsPresenter.presentItem(((DocumentImpl) currentItem).getDoc());
            } else {
              presentResources(currentItem);
            }

          }
        };
        
        
        
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

    //Create a document in the current folder
    menu.add(new CreateDocumentAction(currentParent, currentParent, this));
    //Create a folder in the current folder
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

    menu.add(new PasteDocumentAction(selectedResource ,currentParent, this));
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
    // Get the instance
    CMISAccess instance = CMISAccess.getInstance();

    // Connect
    instance.connect(connectionInfo, repositoryID);

    // Get the rootFolder and set the model
    ResourceController resourceController = instance.createResourceController();
    Folder rootFolder = resourceController.getRootFolder();

    // resourceController.createFolder(rootFolder, "Un nume lung cat o zi de
    // post");

    final FolderImpl origin = new FolderImpl(rootFolder);
    setFolder(origin);

  }

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
    // TODO Auto-generated method stub

  }

  @Override
  public void presentFolderItems(String folderID) {
    // TODO Auto-generated method stub
    ResourceController resourceController = CMISAccess.getInstance().createResourceController();
    presentResources(new FolderImpl(resourceController.getFolder(folderID)));
  }

}
