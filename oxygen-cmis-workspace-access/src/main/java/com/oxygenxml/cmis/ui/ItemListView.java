package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import org.apache.chemistry.opencmis.client.api.Folder;
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
  private DefaultListModel<Repository> model;
  private List<Repository> repoList;
  private TabsPresenter tabsPresenter;
  private BreadcrumbPresenter breadcrumbPresenter;

  ItemListView(TabsPresenter tabsPresenter, BreadcrumbPresenter breadcrumbPresenter) {
    
    // Initialize the tabsPresenter
    this.tabsPresenter = tabsPresenter;
    
    
    // Create the listItem
    resourceList = new JList();
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
          JPopupMenu menu = new JPopupMenu();
          JMenuItem item = new JMenuItem("Say hello");
          JMenuItem editItem = new JMenuItem("Edit");

          editItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev) {
              // Get the location of the item using location of the click
              int itemIndex = resourceList.locationToIndex(e.getPoint());
              IResource currentItem = resourceList.getModel().getElementAt(itemIndex);
              
              String urlAsTring = CustomProtocolExtension.getCustomURL(((DocumentImpl) currentItem).getDoc(),  CMISAccess.getInstance().createResourceController());
              
              System.out.println(urlAsTring);
              
              PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
              if (pluginWorkspace != null) {
                try {
                  pluginWorkspace.open(
                      new URL(urlAsTring));
                } catch (MalformedURLException e1) {
                  // TODO Auto-generated catch block
                  e1.printStackTrace();
                }
              }
            }

          });

          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              JOptionPane.showMessageDialog(ItemListView.this, "Hello " + resourceList.getSelectedValue());
            }

          });
          menu.add(item);
          menu.add(editItem);
          menu.show(ItemListView.this, 10,
              resourceList.getCellBounds(resourceList.getSelectedIndex(), resourceList.getSelectedIndex()).y);
        }

        // Get the location of the item using location of the click
        int itemIndex = resourceList.locationToIndex(e.getPoint());
        // Check if user clicked two times
        if (itemIndex != -1 && e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {

          IResource currentItem = resourceList.getModel().getElementAt(itemIndex);
          
          // Check whether the item in the list
          if (itemIndex != -1) {
            System.out.println("TO present breadcrumb="+currentItem.getDisplayName());
            
            if (!(currentItem instanceof DocumentImpl)) {
              breadcrumbPresenter.presentBreadcrumb(currentItem);
            }
            
            
   
            /*
             * If it's an document show it on a tab instead of iterating the
             * children
             */
            if (currentItem instanceof DocumentImpl) {
              tabsPresenter.presentItem(((DocumentImpl) currentItem).getDoc());
            }else{
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
    
//    resourceController.createFolder(rootFolder, "Un nume lung cat o zi de post");

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
