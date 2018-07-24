package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.oxygenxml.cmis.core.model.IResource;

public class BreadcrumbView extends JPanel implements BreadcrumbPresenter {
  private JPanel toolBar;
  private ItemsPresenter itemsPresenter;

  /*
   * The stack that takes care of the order
   */
  private Stack<IResource> parentResources;

  BreadcrumbView(ItemsPresenter itemsPresenter) {
    this.itemsPresenter = itemsPresenter;
    parentResources = new Stack<IResource>();

    toolBar = new JPanel();

//    toolBar.setFloatable(false);
//    toolBar.setRollover(true);

    JScrollPane scrollingBreadcrumb = new JScrollPane(toolBar);

    Border emptyBorder = BorderFactory.createEmptyBorder();
    scrollingBreadcrumb.setHorizontalScrollBar(scrollingBreadcrumb.createHorizontalScrollBar());
    //scrollingBreadcrumb.setBorder(emptyBorder);

    toolBar.addComponentListener(new ComponentAdapter( ) {
     public void componentResized(ComponentEvent e) {
       // java - get screen size using the Toolkit class
       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
       
       System.out.println("Toolbar size: " + toolBar.getSize());
       Component[] components = toolBar.getComponents();
       for (int i = 0; i < components.length; i++) {
        System.out.println("Child " + components[i].getSize());
      }
     }
     });

    JPanel breadcrumbPanel = new JPanel();
    JLabel goUpIcon = new JLabel();
    goUpIcon.setIcon(UIManager.getIcon("FileChooser.upFolderIcon"));

    // Set layout
    setLayout(new BorderLayout());
    breadcrumbPanel.setLayout(new GridBagLayout());
    // goUpIcon.setBackground(Color.RED);
    // toolBar.setBackground(Color.blue);
    GridBagConstraints c = new GridBagConstraints();

    // GoUpIcon
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;

    breadcrumbPanel.add(goUpIcon, c);

    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1.0;
    c.anchor = GridBagConstraints.BASELINE_TRAILING;
    //c.fill = GridBagConstraints.HORIZONTAL;
    breadcrumbPanel.add(toolBar, c);
    
    toolBar.setBackground(Color.YELLOW);

    add(breadcrumbPanel, BorderLayout.CENTER);
  }

  /*
   * Custom JButton for JToolbar
   */
  public JButton customJButton(final IResource resource) {
    JButton currentButton = new JButton(resource.getDisplayName() + "> ");

    currentButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JButton currentItem = (JButton) e.getSource();

        // While goes back to the target selected pop elements
        while (!resource.getId().equals(parentResources.peek().getId())) {
          System.out.println("Eliminate: " + parentResources.peek().getDisplayName());
          toolBar.remove(toolBar.getComponentCount() - 1);
          parentResources.pop();
        }

        IResource itemToShow = parentResources.peek();
        boolean checkStack = parentResources.isEmpty();

        if (!checkStack) {
          System.out.println("To present in breadcrumb=" + parentResources.peek().getDisplayName());
          itemsPresenter.presentFolderItems(parentResources.peek().getId());
          toolBar.remove(toolBar.getComponentCount() - 1);
          parentResources.pop();

          toolBar.repaint();
          presentBreadcrumb(itemToShow);
        }

      }
    });
    
    System.out.println("Button pref size: " + currentButton.getPreferredSize());

    return currentButton;
  }

  /*
   * Present the breadcrumb
   */
  @Override
  public void presentBreadcrumb(IResource resource) {

    parentResources.push(resource);
    System.out.println("Go to breadcrumb=" + parentResources.peek().getDisplayName());

    // Add to the toolBar
    JButton customJButton = customJButton(resource);
    
    toolBar.add(customJButton);

    // Revalidate to not show an empty component
    getParent().revalidate();
    getParent().repaint();
    
    System.out.println("Actual size: " + customJButton.getSize());
  }

  /*
   * Reset the whole breadcrumb and data from it
   */
  @Override
  public void resetBreadcrumb(boolean flag) {
    if (flag) {
      // Remove old data

      parentResources.removeAllElements();
      toolBar.removeAll();

      // Revalidate to not show an empty component
      getParent().revalidate();
      getParent().repaint();
    }

  }

}
