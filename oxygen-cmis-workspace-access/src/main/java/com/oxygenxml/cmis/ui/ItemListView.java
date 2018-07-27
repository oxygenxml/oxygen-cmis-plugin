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

        if (SwingUtilities.isRightMouseButton(e)) {
          menu = new JPopupMenu();
          // Get the location of the item using location of the click
          int itemIndex = resourceList.locationToIndex(e.getPoint());
          IResource currentItem = resourceList.getModel().getElementAt(itemIndex);

          // TODO create a PopUpMenu for a document and for a folder
          JMenuItem editItem = new JMenuItem("Edit");

          editItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev) {
              
            	String urlAsTring = null;
            	
				try {
					urlAsTring = CustomProtocolExtension.getCustomURL(((DocumentImpl) currentItem).getDoc(),
					  CMISAccess.getInstance().createResourceController());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

              System.out.println(urlAsTring);

              PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
              if (pluginWorkspace != null) {
                try {
                  pluginWorkspace.open(new URL(urlAsTring));
                } catch (MalformedURLException e1) {
                  // TODO Auto-generated catch block
                  e1.printStackTrace();
                }
              }
            }

          });

          menu.add(editItem);
          if (currentItem instanceof DocumentImpl) {

            createDocumentJMenu(e);

          } else if (currentItem instanceof FolderImpl) {

            createFolderJMenu(e);

          }

          // UI feedback
          // Bounds rectangle
          Rectangle boundsRec = resourceList.getCellBounds(resourceList.getSelectedIndex(),
              resourceList.getSelectedIndex());

          if (boundsRec == null) {

            JOptionPane.showMessageDialog(ItemListView.this, "Please select an item ");
          } else {

            menu.show(ItemListView.this, 10, boundsRec.y);
          }

        }

        // Get the location of the item using location of the click
        int itemIndex = resourceList.locationToIndex(e.getPoint());
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

  /**
   * get string from Clipboard
   */
  private String getSysClipboardText() {
    String ret = "";
    Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    Transferable clipTf = sysClip.getContents(null);

    if (clipTf != null) {

      if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        try {
          ret = (String) clipTf.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    return ret;
  }

  /**
   * put string into Clipboard
   */
  private void setSysClipboardText(String writeMe) {
    Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable tText = new StringSelection(writeMe);
    clip.setContents(tText, null);
  }

  /*
   * JMenu for the Document
   */
  private void createDocumentJMenu(final MouseEvent e) {

    // CRUD Document
    JMenuItem copyDoc = new JMenuItem("Copy");
    JMenuItem deleteDoc = new JMenuItem("Delete");
    JMenuItem moveDoc = new JMenuItem("Move to");
    JMenuItem checkInDoc = new JMenuItem("Check In");
    JMenuItem checkOutDoc = new JMenuItem("Check Out");
    JMenuItem cancelCheckOutDoc = new JMenuItem("Cancel check out");

    // CRUD Document listeners

    // Copy doc
    copyDoc.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        // Get the location of the item using location of the click
        int itemIndex = resourceList.locationToIndex(e.getPoint());
        IResource currentItem = resourceList.getModel().getElementAt(itemIndex);

        DocumentImpl doc = ((DocumentImpl) currentItem);

        String textClipboard = "";

        /*
         * A reader to reader 1024 characters using a ResourceController and
         * document ID
         */
        Reader documentContent = null;
        try {
          documentContent = CMISAccess.getInstance().createResourceController().getDocumentContent(doc.getId());
          char[] ch = new char[1024];
          int l = -1;

          // While there is text to read
          while ((l = documentContent.read(ch)) != -1) {

            // Append the text to the clipboard
            textClipboard += String.valueOf(ch, 0, l);

            // Set the clipboard
            setSysClipboardText(textClipboard);
          }
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();

        } catch (IOException e) {
          e.printStackTrace();

        } finally {
          // No matter what happens close at the end the doc

          // Even if it's not empty
          if (documentContent != null) {

            try {
              documentContent.close();

            } catch (IOException e) {

              e.printStackTrace();
            }
          }
        }

      }
    });
    
    //TODO Check if is removed one reference or all maybe use of removeFromFolder
    
    // Delete doc
    deleteDoc.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        // Get the location of the item using location of the click
        int itemIndex = resourceList.locationToIndex(e.getPoint());
        IResource currentItem = resourceList.getModel().getElementAt(itemIndex);

        DocumentImpl doc = ((DocumentImpl) currentItem);

        try {
          CMISAccess.getInstance().createResourceController().deleteOneVersionDocument(doc.getDoc());
        } catch (Exception e) {
          JOptionPane.showMessageDialog(ItemListView.this, "Exception " + e.getMessage());
        }
      }

    });
    
    //TODO Drag and drop
    // Move to Folder
//    moveDoc.addActionListener(new ActionListener() {
//
//      @Override
//      public void actionPerformed(ActionEvent ev) {
//        // Get the location of the item using location of the click
//        int itemIndex = resourceList.locationToIndex(e.getPoint());
//        IResource currentItem = resourceList.getModel().getElementAt(itemIndex);
//
//        DocumentImpl doc = ((DocumentImpl) currentItem);
//        FolderImpl sourceFolder = BreadcrumbView.currentFolder;
//        String stringTargetFolder = (String)JOptionPane.showInputDialog(
//            ItemListView.this,
//            "Complete the sentence:\n"
//            + "\"Green eggs and...\"",
//            "Customized Dialog",
//            JOptionPane.PLAIN_MESSAGE,
//            null, null, "ham");
//        
//        FolderImol targetFolder = CMISAccess.getInstance().createResourceController().get
//        try {
//          CMISAccess.getInstance().createResourceController().move(sourceFolder, targetFolder, doc.getDoc());
//        } catch (Exception e) {
//          JOptionPane.showMessageDialog(ItemListView.this, "Exception " + e.getMessage());
//        }
//      }
//
//    });
    
    // Check In doc
    checkInDoc.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        // Get the location of the item using location of the click
        int itemIndex = resourceList.locationToIndex(e.getPoint());
        IResource currentItem = resourceList.getModel().getElementAt(itemIndex);
        ObjectId res = null;
        DocumentImpl doc = ((DocumentImpl) currentItem);

        try {
          res = (ObjectId) doc.checkIn(doc.getDoc());
        } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException e) {
          JOptionPane.showMessageDialog(ItemListView.this, "Exception " + e.getMessage());
        }

      }
    });

    // CheckOutDoc
    checkOutDoc.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        // Get the location of the item using location of the click
        int itemIndex = resourceList.locationToIndex(e.getPoint());
        IResource currentItem = resourceList.getModel().getElementAt(itemIndex);

        Document res = null;
        DocumentImpl doc = ((DocumentImpl) currentItem);
        try {
          res = doc.checkOut(doc.getDoc(), doc.getDocType());

        } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException e) {
          JOptionPane.showMessageDialog(ItemListView.this, "Exception " + e.getMessage());
        }

        System.out.println(res);
      }
    });

    // cancelCheckOutDoc
    cancelCheckOutDoc.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        // Get the location of the item using location of the click
        int itemIndex = resourceList.locationToIndex(e.getPoint());
        IResource currentItem = resourceList.getModel().getElementAt(itemIndex);

        DocumentImpl doc = ((DocumentImpl) currentItem);
        try {

          doc.cancelCheckOut(doc.getDoc());
        } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException e) {
          JOptionPane.showMessageDialog(ItemListView.this, "Exception " + e.getMessage());
        }

      }
    });
    menu.add(deleteDoc);
    menu.add(checkInDoc);
    menu.add(checkOutDoc);
    menu.add(cancelCheckOutDoc);
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

    // Delete Folder tree
    deleteFolder.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        // Get the location of the item using location of the click
        int itemIndex = resourceList.locationToIndex(e.getPoint());
        IResource currentItem = resourceList.getModel().getElementAt(itemIndex);
        ObjectId res = null;

        FolderImpl folder = ((FolderImpl) currentItem);

        try {
          CMISAccess.getInstance().createResourceController().deleteFolderTree(folder.getFolder());
        } catch (Exception e) {
          JOptionPane.showMessageDialog(ItemListView.this, "Exception " + e.getMessage());
        }

      }
    });
    //TODO check out all resources
    // CheckOutAll
//    checkOutDoc.addActionListener(new ActionListener() {
//
//      @Override
//      public void actionPerformed(ActionEvent ev) {
//        // Get the location of the item using location of the click
//        int itemIndex = resourceList.locationToIndex(e.getPoint());
//        IResource currentItem = resourceList.getModel().getElementAt(itemIndex);
//
//        Document res = null;
//        DocumentImpl doc = ((DocumentImpl) currentItem);
//        try {
//          res = doc.checkOut(doc.getDoc(), doc.getDocType());
//
//        } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException e) {
//          JOptionPane.showMessageDialog(ItemListView.this, "Exception " + e.getMessage());
//        }
//
//        System.out.println(res);
//      }
//    });
    
    
    //TODO cancel check out all resources
    // cancelCheckOutDoc
//    cancelCheckOutDoc.addActionListener(new ActionListener() {
//
//      @Override
//      public void actionPerformed(ActionEvent ev) {
//        // Get the location of the item using location of the click
//        int itemIndex = resourceList.locationToIndex(e.getPoint());
//        IResource currentItem = resourceList.getModel().getElementAt(itemIndex);
//
//        DocumentImpl doc = ((DocumentImpl) currentItem);
//        try {
//
//          doc.cancelCheckOut(doc.getDoc());
//        } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException e) {
//          JOptionPane.showMessageDialog(ItemListView.this, "Exception " + e.getMessage());
//        }
//
//      }
//    });

    menu.add(deleteFolder);
//    menu.add(checkOutDoc);
//    menu.add(cancelCheckOutDoc);
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
   *          The resource to present its children.
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
