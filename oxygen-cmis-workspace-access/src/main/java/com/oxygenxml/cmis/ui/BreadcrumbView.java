package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.oxygenxml.cmis.core.model.IResource;

import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;

public class BreadcrumbView extends JPanel implements BreadcrumbPresenter {
  private JToolBar toolBar;
  private ToolbarButton popUpButton;
  private JPanel breadcrumbPanel;
  private JLabel goUpIcon;
  private ItemsPresenter itemsPresenter;

  /*
   * The stack that takes care of the order
   */
  private Stack<IResource> parentResources;
  private Stack<JButton> hiddenItems;

  BreadcrumbView(ItemsPresenter itemsPresenter) {
    // Initialize data
    this.itemsPresenter = itemsPresenter;
    parentResources = new Stack<IResource>();
    hiddenItems = new Stack<JButton>();

    toolBar = new JToolBar();
    breadcrumbPanel = new JPanel();
    goUpIcon = new JLabel();

    // Design the toolbar
    toolBar.setFloatable(false);
    toolBar.setRollover(true);

    // Add listener to the toolbar
    toolBar.addComponentListener(new ComponentAdapter() {

      public void componentResized(ComponentEvent e) {

        doBreadcrumbsLayout();
      }


    });

    // Set the icon
    goUpIcon.setIcon(UIManager.getIcon("FileChooser.upFolderIcon"));

    // Set layout
    setLayout(new BorderLayout());
    breadcrumbPanel.setLayout(new GridBagLayout());

    // Set constraints
    GridBagConstraints c = new GridBagConstraints();

    // GoUpIcon
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;

    breadcrumbPanel.add(goUpIcon, c);

    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1.0;
    // c.anchor = GridBagConstraints.BASELINE_LEADING;
    c.fill = GridBagConstraints.HORIZONTAL;
    breadcrumbPanel.add(toolBar, c);

    toolBar.setBackground(Color.YELLOW);
    add(breadcrumbPanel, BorderLayout.CENTER);
  }

  /*
   * Custom JButton for JToolbar
   */
  public JButton customJButton(final IResource resource) {
    Action action = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        // While goes back to the target selected pop elements
        // Remove descendants from the visible toolbar.
        while (!resource.getId().equals(parentResources.peek().getId())
            && toolBar.getComponentCount() > 0) {
          System.out.println("Eliminate: " + parentResources.peek().getDisplayName());
          toolBar.remove(toolBar.getComponentCount() - 1);
          parentResources.pop();
        }
        
        if (toolBar.getComponentCount() == 0) {
          while(hiddenItems.peek() != null) {
            JButton peek = hiddenItems.peek();
            IResource pop = parentResources.peek();
            if (resource.getId().equals(pop.getId())) {
              
              hiddenItems.pop();
              break;
            } else {
              parentResources.pop();
              hiddenItems.pop();
            }
            
          }
        }


        if (!parentResources.isEmpty()) {
//          IResource itemToShow = parentResources.peek();
//          breadcrumbToPresent(itemToShow);
          itemsPresenter.presentFolderItems(parentResources.peek().getId());
        }
//        
        doBreadcrumbsLayout();
        toolBar.revalidate();
        toolBar.repaint();
      }
    };

    // Create a button using Oxygen button class
    action.putValue(Action.NAME, resource.getDisplayName());
    ToolbarButton currentButton = new ToolbarButton(action, true);
    System.out.println("Button pref size: " + currentButton.getPreferredSize());

    return currentButton;
  }

  /*
   * Present the item in the breadcrumb
   */
  private void breadcrumbToPresent(IResource itemToShow) {
    System.out.println("To present in breadcrumb=" + parentResources.peek().getDisplayName());
    itemsPresenter.presentFolderItems(parentResources.peek().getId());
    if (toolBar.getComponentCount() > 0) {
      toolBar.remove(toolBar.getComponentCount() - 1);
      parentResources.pop();
    }

    toolBar.repaint();
    presentBreadcrumb(itemToShow);
  }

  /*
   * popUp JButton for JToolbar
   */
  public JButton popUpJButton(final String resource) {
    Action action = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
         JPopupMenu popUpMenu = new JPopupMenu();
         
        for (JButton jButton : hiddenItems) {
          JMenuItem menuItem = new JMenuItem(jButton.getAction());
          popUpMenu.add(menuItem, 0);
        }

        // Add to the popUpMenu
        Component source = (Component) e.getSource();
        popUpMenu.show(source, 0, source.getHeight());

      }
    };

    action.putValue(Action.NAME, resource);
    ToolbarButton popUpButton = new ToolbarButton(action, true);

    return popUpButton;
  }

  /*
   * Present the breadcrumb
   */
  @Override
  public void presentBreadcrumb(IResource resource) {

    parentResources.push(resource);
    System.out.println("Go to breadcrumb=" + parentResources.peek().getDisplayName());

    // Add to the toolBar the popUp
    // ----------------------------
    JButton customJButton = customJButton(resource);
    System.out.println("Preferred size=" + toolBar.getPreferredSize());

    boolean hasEnoughWidth = hasEnoughWidth(customJButton);
    if (!hasEnoughWidth) {
      // TODO What to do????

      // Add it to the popupButton
      JMenuItem item = new JMenuItem("Say hello");

      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {

          // To present if was clicked
          breadcrumbToPresent(resource);
        }
      });

    }

    toolBar.add(customJButton);
    // ----------------------------

    // Revalidate to not show an empty component
    getParent().revalidate();
    getParent().repaint();

    System.out.println("Actual size: " + customJButton.getSize());
  }

  private boolean hasEnoughWidth(JButton customJButton) {
    boolean hasWidth = (getComponentsWidth(toolBar) + customJButton.getPreferredSize().getWidth()) < toolBar.getWidth();
    return hasWidth;
  }

  /*
   * Reset the whole breadcrumb and data from it
   */
  @Override
  public void resetBreadcrumb(boolean flag) {
    if (flag) {
      // Remove old data

      parentResources.removeAllElements();
      hiddenItems.removeAllElements();
      toolBar.removeAll();
      // Revalidate to not show an empty component
      getParent().revalidate();
      getParent().repaint();
    }

  }

  /*
   * @return integer width of components from JToolBar
   */
  int getComponentsWidth(JToolBar toolbar) {
    int totalWidth = 0;
    Component[] components = toolBar.getComponents();
    for (int i = 0; i < components.length; i++) {
      totalWidth += components[i].getWidth();
    }
    return totalWidth;
  }
  
  
  private void doBreadcrumbsLayout() {
    System.out.println("Toolbar size: " + toolBar.getSize());
    Component[] components = toolBar.getComponents();
    for (int i = 0; i < components.length; i++) {
      System.out.println("Child " + components[i].getSize());
    }


    boolean existsMoreWidget = false;
    if (toolBar.getComponentCount() > 0) {
      JButton first = (JButton) toolBar.getComponent(0);
      if (first.getText().equals("..")) {
        existsMoreWidget = true;
      }
    }

    int counter = existsMoreWidget ? 1 : 0;
    while (getComponentsWidth(toolBar) > toolBar.getWidth()) {

      hiddenItems.push((JButton) toolBar.getComponentAtIndex(counter));
      toolBar.remove(counter);
      
    }

    int componentsWidth = getComponentsWidth(toolBar);
    while (componentsWidth < toolBar.getWidth() && !hiddenItems.isEmpty()) {
      JButton item = hiddenItems.peek();
      double childWidth = item.getPreferredSize().getWidth();
      componentsWidth += childWidth;
      if (componentsWidth < toolBar.getWidth()) {
        toolBar.add(item, counter);
      
        hiddenItems.pop();
      }
    }


    // Add the "more items" widget if needed.
    if (!hiddenItems.isEmpty() && !existsMoreWidget) {
      // This will break the UI
      toolBar.add(popUpJButton(".."), 0);
    } else if (hiddenItems.isEmpty() && existsMoreWidget) {
      toolBar.remove(0);
    }
  }

}
