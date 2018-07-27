package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Repository;

import com.oxygenxml.cmis.actions.OpenDocumentAction;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.core.urlhandler.CustomProtocolExtension;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class ItemListView extends JPanel implements ItemsPresenter, ListSelectionListener {

  private JList<IResource> resourceList;
  private JPopupMenu menu;

  ItemListView(TabsPresenter tabsPresenter, BreadcrumbPresenter breadcrumbPresenter) {

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
        
        if (SwingUtilities.isRightMouseButton(e)) {
          menu = new JPopupMenu();
          
//          IResource currentResource = getResource(e.getPoint());
//          
//          menu.add(new CopyAction(currentResource));
//          menu.add(new PasteAction(currentResource))

          // TODO CReate a Copy Action that extends AbstractAction


          // Get the location of the item using location of the click
          IResource currentItem = resourceList.getModel().getElementAt(itemIndex);

          if (currentItem instanceof DocumentImpl) {

            createDocumentJMenu(currentItem);

          } else if (currentItem instanceof FolderImpl) {

            createFolderJMenu(e);

          }
//          menu.addSeparator();
//          menu.add(createDoc);
//          menu.add(createFolder);
//          menu.addSeparator();
//          menu.add(pasteDoc);
//          menu.add(pasteFolder);

          // Bounds of the click
          menu.show(ItemListView.this, e.getX(), e.getY());

        }

       
        // Check if user clicked two times
        if (itemIndex != -1 && e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {

          IResource currentItem = resourceList.getModel().getElementAt(itemIndex);

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
        }
      }
    });

    // Set layout
    setLayout(new BorderLayout(0, 0));

    add(listItemScrollPane, BorderLayout.CENTER);

  }


  /*
   * JMenu for the Document
   */
  private void createDocumentJMenu(final IResource resource) {
    // CRUD Document
    menu.add(new OpenDocumentAction("Open document", resource));
    
//    menu.add(deleteDoc);
//    menu.add(checkInDoc);
//    menu.add(checkOutDoc);
//    menu.add(cancelCheckOutDoc);

    // CRUD Document listeners

    // TODO Check if is removed one reference or all maybe use of
    // removeFromFolder

    // TODO Drag and drop
    // Move to Folder
  }

  /*
   * JMenu for the Folder
   */

  private void createFolderJMenu(final MouseEvent e) {

    // CRUD Folder
    JMenuItem copyFolder = new JMenuItem("Copy");
    JMenuItem deleteFolder = new JMenuItem("Delete");
    JMenuItem checkOutDoc = new JMenuItem("Check Out");
    JMenuItem cancelCheckOutDoc = new JMenuItem("Cancel check out");

    // CRUD Folder listeners

    // TODO copy all resources
    // copy all
    
    // TODO check out all resources
    // CheckOutAll
  

    // TODO cancel check out all resources
    // cancelCheckOutDoc
  

    menu.add(deleteFolder);
    // menu.add(checkOutDoc);
    // menu.add(cancelCheckOutDoc);
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
  private void presentResources(IResource resource) {
    System.out.println("Current item=" + resource.getDisplayName());
    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    /*
     * Iterate them till it has a child
     */
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
