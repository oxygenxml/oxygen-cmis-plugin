package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

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
    breadcrumbList.setModel(new DefaultListModel<>());
    parentResources = new Stack<IResource>();
    
    
    
    // Set the list to be HORIZONTAL
    breadcrumbList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    breadcrumbList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    breadcrumbList.setVisibleRowCount(-1);
    
    JScrollPane scrollingBreadcrumb = new JScrollPane(breadcrumbList);
    
    Border emptyBorder = BorderFactory.createEmptyBorder();
    scrollingBreadcrumb.setBorder(emptyBorder);
    
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

          renderTex = ((IResource) value).getDisplayName()+">";

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
            System.out.println("Clicked resource: " + currentItem.getDisplayName());
            // While goes back to the target selected pop elements
            while (!currentItem.getId().equals(parentResources.peek().getId())) {

              System.out.println("Eliminate: " + parentResources.peek().getDisplayName());
              parentResources.pop();
            }
            
            IResource itemToShow = parentResources.peek();
            boolean checkStack = parentResources.isEmpty();
            
            if(!checkStack){
              System.out.println("To present in breadcrumb=" + parentResources.peek().getDisplayName());
              itemsPresenter.presentFolderItems(parentResources.peek().getId());
              
              parentResources.pop();
              
              presentBreadcrumb(itemToShow);
            }
            

          }
        }
      }
    });

    // Set layout
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    //constraints
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.9;
    c.gridwidth = 3;
    c.gridx = 0;
    c.gridy = 0;
    c.ipadx = 50;
    c.ipady = 20;
    add(scrollingBreadcrumb, c);
   
  }

  @Override
  public void presentBreadcrumb(IResource resource) {

    parentResources.push(resource);
    System.out.println("Go to breadcroumb=" + parentResources.peek().getDisplayName());
    

    System.out.println("Current breadcrumb=" + resource.getDisplayName());

    // Define a model for the list in order to render the items
    DefaultListModel<IResource> model = new DefaultListModel<>();
    for (IResource iResource : parentResources) {
      model.addElement(iResource);
      
    }
    
    breadcrumbList.setModel(model);

  }

}
