package com.oxygenxml.cmis.ui;

import java.awt.event.ActionEvent;
import java.util.Deque;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.model.IResource;

public class BreadcrumbButtonAction extends AbstractAction {
  private static final Logger logger = Logger.getLogger(BreadcrumbButtonAction.class);
  private final transient IResource resource;
  private final transient Deque<IResource> parentResources;
  private final transient Deque<JButton> hiddenItems;
  private final transient JToolBar breadcrumbToolBar;
  private final transient ResourcesBrowser itemsPresenter;

  BreadcrumbButtonAction(IResource resource, Deque<IResource> parentResources, Deque<JButton> hiddenItems,
      JToolBar breadcrumbToolBar, ResourcesBrowser itemsPresenter) {
    super(resource.getDisplayName());

    this.resource = resource;
    this.parentResources = parentResources;
    this.hiddenItems = hiddenItems;
    this.breadcrumbToolBar = breadcrumbToolBar;
    this.itemsPresenter = itemsPresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // While goes back to the target selected pop elements, remove
    // descendants from the visible toolbar.
    while (!resource.getId().equals(parentResources.peek().getId()) && breadcrumbToolBar.getComponentCount() > 0) {

      if (logger.isDebugEnabled()) {
        logger.debug("Eliminate: " + parentResources.peek().getDisplayName());
      }

      breadcrumbToolBar.remove(breadcrumbToolBar.getComponentCount() - 1);
      parentResources.pop();
    }

    if (breadcrumbToolBar.getComponentCount() == 0) {
      // Remove from the stack all the descendants of the clicked item.
      while (hiddenItems.peek() != null) {

        IResource pop = parentResources.peek();

        // Check if reached the target by ID
        if (resource.getId().equals(pop.getId())) {
          hiddenItems.pop();
          // Break when found.
          break;
        } else {
          // Pop until we reach the target.
          parentResources.pop();
          hiddenItems.pop();
        }
      }
    }

    // Present the resources (children) of the items.
    if (!parentResources.isEmpty()) {
      itemsPresenter.presentResources(parentResources.peek().getId());
    }

    // Revalidates toolBar view and refreshes it.
    breadcrumbToolBar.revalidate();
    breadcrumbToolBar.repaint();

  }

}
