package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
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

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

public class ItemListView extends JPanel implements ItemsPresenter, ListSelectionListener {
  
  private JList<IResource> resourceList;
  private DefaultListModel<Repository> model;
  private List<Repository> repoList;
  private TabsPresenter tabsPresenter;
  private Folder parentFolder;
  private Stack<IResource> parentResources;
 
  private IResource origin = null;

  ItemListView(TabsPresenter tabsPresenter) {
    // Initialize the tabsPresenter
    this.tabsPresenter = tabsPresenter;
   
    parentResources = new Stack<IResource>();
    // Create the listItem
    resourceList = new JList();
    resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    resourceList.setSelectedIndex(0);
    resourceList.addListSelectionListener(this);

    
    // Scroller for the listRepo
    JScrollPane listItemScrollPane = new JScrollPane(resourceList);

    // Setting the minum size for the SideList and MainList
    Dimension minimumSizeSideList = new Dimension(300, 100);
    listItemScrollPane.setMinimumSize(minimumSizeSideList);

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
      public void mouseClicked(MouseEvent e) {

        if (SwingUtilities.isRightMouseButton(e)) {
          JPopupMenu menu = new JPopupMenu();
          JMenuItem item = new JMenuItem("Say hello");
          JMenuItem editItem = new JMenuItem("Edit");

          editItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
              // TODO Auto-generated method stub

            }

          });

          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              JOptionPane.showMessageDialog(ItemListView.this, "Hello " + resourceList.getSelectedValue());
            }

          });
          menu.add(item);
          menu.show(ItemListView.this, 10,
              resourceList.getCellBounds(resourceList.getSelectedIndex(), resourceList.getSelectedIndex()).y);
        }

        // Check if user clicked two times
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {

          // Get the location of the item using location of the click
          int itemIndex = resourceList.locationToIndex(e.getPoint());
          IResource currentItem = resourceList.getModel().getElementAt(itemIndex);
          // Check whether the item in the list
          if (itemIndex != -1) {
            

            presentResources(currentItem);

            /*
             * If it's an document show it on a tab instead of iterating the
             * children
             */
            if (currentItem instanceof DocumentImpl) {
              tabsPresenter.presentItem(((DocumentImpl) currentItem).getDoc());
            }

          }
        }
      }
    });

    // Set layout
    setLayout(new BorderLayout());
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
    Folder rootFolder = instance.createResourceController().getRootFolder();

    final FolderImpl origin = new FolderImpl(rootFolder);

    DefaultListModel<IResource> model = new DefaultListModel<>();
    parentResources.push(new IResource() {
      @Override
      public Iterator<IResource> iterator() {
        return Arrays.asList(new IResource[] {origin}).iterator();
      }
      
      @Override
      public String getId() {
        return "";
      }
      
      @Override
      public String getDisplayName() {
        return "..";
      }
    });
    
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
    
    
    if (resource instanceof GoUpResource) {
      // Go one level up.
      parentResources.pop();
      System.out.println("Go to=" + parentResources.peek().getDisplayName());
    } 
    
    

    System.out.println("Current item=" + resource.getDisplayName());
    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    /*
     * Iterate them till it has a child
     */
    if (childrenIterator != null && childrenIterator.hasNext()) {
      
      if (!(resource instanceof GoUpResource)) {

        // The case when I go inside a folder.
        System.out.println(resource.getDisplayName());
        
        // Push the parent into the stack.
        parentResources.push(resource);
      } 

      // Define a model for the list in order to render the items
      DefaultListModel<IResource> model = new DefaultListModel<>();

      if (parentResources.size() > 1) {
        model.addElement(new GoUpResource(parentResources));
      }
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
  
  
  private class GoUpResource implements IResource {
    
    private Stack<IResource> parentResources;
    
    private GoUpResource(Stack<IResource> parentResources) {
      this.parentResources = parentResources;
    }
    
    @Override
    public Iterator<IResource> iterator() {
      IResource wrapped = parentResources.peek();
      return wrapped .iterator();
    }

    @Override
    public String getDisplayName() {
      return "..";
    }

    @Override
    public String getId() {
      return parentResources.peek().getId();
    }
  }

}
