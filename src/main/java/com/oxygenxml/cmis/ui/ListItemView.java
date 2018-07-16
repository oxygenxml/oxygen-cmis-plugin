package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

public class ListItemView extends JPanel implements ItemsPresenter, ListSelectionListener {
  private JList<IResource> listItem;
  private DefaultListModel<Repository> model;
  private List<Repository> repoList;
  private TabsPresenter tabsPresenter;

  ListItemView(TabsPresenter tabsPresenter) {
    // Initialize the tabsPresenter
    this.tabsPresenter = tabsPresenter;

    // Create the listItem
    listItem = new JList();
    listItem.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listItem.setSelectedIndex(0);
    listItem.addListSelectionListener(this);

    // Scroller for the listRepo
    JScrollPane listItemScrollPane = new JScrollPane(listItem);

    // Setting the minum size for the SideList and MainList
    Dimension minimumSizeSideList = new Dimension(200, 100);
    listItemScrollPane.setMinimumSize(minimumSizeSideList);

    /*
     * Render all the elements of the listItem when necessary
     */
    listItem.setCellRenderer(new DefaultListCellRenderer() {
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
    listItem.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {

        // Check if user clicked two times
        if (e.getClickCount() == 2) {
          
          // Get the location of the item using location of the click
          int itemIndex = listItem.locationToIndex(e.getPoint());

          // Check whether the item in the list
          if (itemIndex != -1) {
            IResource currentItem = listItem.getModel().getElementAt(itemIndex);
            
            // Get all the children of the item in an iterator
            Iterator<IResource> childrenIterator = currentItem.iterator();

            /*
             * Iterate them till it has a child
             */
            if (childrenIterator != null && childrenIterator.hasNext()) {
              
              //Define a model for the list in order to render the items
              DefaultListModel<IResource> model = new DefaultListModel<>();

              // While has a child, add to the model
              while (childrenIterator.hasNext()) {
                IResource iResource = (IResource) childrenIterator.next();
                model.addElement(iResource);

              }
              // Set the model to the list
              listItem.setModel(model);

            }

            /*
             *  If it's an document show it on a tab instead of iterating the children
             */
            if (currentItem instanceof DocumentImpl) {
              tabsPresenter.presentItem(((DocumentImpl) currentItem).getDoc());
            }

          }
        }
      }
    });

    //Set layout
    setLayout(new BorderLayout());
    add(listItemScrollPane, BorderLayout.CENTER);
  }
/*
 * Implemented presentItems
 * using connectionInfo and repoID 
 * !! Model shall be created whenever the list is updated
 * Facade Pattern
 */
  @Override
  public void presentItems(URL connectionInfo, String repositoryID) {
    // Get the instance
    CMISAccess instance = CMISAccess.getInstance();
    // Connect
    instance.connect(connectionInfo, repositoryID);
    
    // Get the rootFolder and set the model
    Folder rootFolder = instance.createResourceController().getRootFolder();
    FolderImpl resource = new FolderImpl(rootFolder);

    DefaultListModel<IResource> model = new DefaultListModel<>();
    model.addElement(resource);
    listItem.setModel(model);

  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    // TODO Auto-generated method stub

  }

}
