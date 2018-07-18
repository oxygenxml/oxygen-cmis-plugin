package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.oxygenxml.cmis.core.model.IResource;

public class BreadcrumbView extends JPanel implements BreadcrumbPresenter {

  private ItemsPresenter itemsPresenter;

  JList<IResource> breadcrumbList;
  /*
   * The stack that takes care of the order
   */
  private Stack<IResource> parentResources;

  BreadcrumbView(ItemsPresenter itemsPresenter) {
    this.itemsPresenter = itemsPresenter;
    breadcrumbList = new JList<IResource>();
    parentResources = new Stack<IResource>();

    // Set the list to be HORIZONTAL
    breadcrumbList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    breadcrumbList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    breadcrumbList.setVisibleRowCount(-1);

    JScrollPane scrollingBreadcrumb = new JScrollPane(breadcrumbList);

    /*
     * Render all the elements of the listItem when necessary
     */
    breadcrumbList.setCellRenderer(new DefaultListCellRenderer() {
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
    breadcrumbList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {

        // Check if user clicked two times
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {

          // Get the location of the item using location of the click
          int targetIndex = breadcrumbList.locationToIndex(e.getPoint());

          IResource currentItem = breadcrumbList.getModel().getElementAt(targetIndex);

          // Check whether the item in the list
          if (targetIndex != -1) {
            System.out.println(currentItem.getDisplayName());
            // While goes back to the target selected pop elements
            while (!currentItem.getId().equals(parentResources.peek().getId())) {
              
              System.out.println(parentResources.peek().getDisplayName());
              parentResources.pop();
            }
            //presentBreadcrumb(parentResources.peek());
            System.out.println(parentResources.peek().getId());
            itemsPresenter.presentFolderItems(parentResources.peek().getId());
            
            
          }
        }
      }
    });

    // Set layout
    setLayout(new BorderLayout());
    add(scrollingBreadcrumb, BorderLayout.CENTER);
  }

  @Override
  public void presentBreadcrumb(IResource resource) {

    parentResources.push(resource);
    System.out.println("Go to breadcroumb=" + parentResources.peek().getDisplayName());

    System.out.println("Current breadcrumb=" + resource.getDisplayName());
    // Get all the children of the item in an iterator
    Iterator<IResource> childrenIterator = resource.iterator();

    // Define a model for the list in order to render the items
    DefaultListModel<IResource> model = new DefaultListModel<>();

    //Iterate the stack
    for (IResource obj : parentResources) {

      model.addElement(obj);

    }
    // Set the model to the list
    breadcrumbList.setModel(model);

  }

}
