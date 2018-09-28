package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Deque;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.plugin.TranslationResourceController;

import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;

/**
 * Presents the path to a resource.
 */
public class BreadcrumbView extends JPanel implements BreadcrumbPresenter, RepositoryListener {

  private static final String GO_BACK_TOOLTIP = TranslationResourceController.getMessage("GO_BACK_TOOLTIP");
  private static final Logger logger = Logger.getLogger(BreadcrumbView.class);
  private final JToolBar breadcrumbToolBar;
  private final JPanel breadcrumbPanel;
  private final JLabel goUpIcon;
  private final transient ResourcesBrowser itemsPresenter;
  private transient FolderImpl currentFolder;

  /*
   * The stack that takes care of the order.
   */
  private final transient Deque<IResource> parentResources = new LinkedList<>();
  private final Deque<JButton> hiddenItems = new LinkedList<>();

  /**
   * Creates the UI.
   * 
   * @param itemsPresenter
   *          Allows brosing in the resources.
   */
  public BreadcrumbView(ResourcesBrowser itemsPresenter) {
    setOpaque(true);

    // Initialize data.
    this.itemsPresenter = itemsPresenter;

    breadcrumbToolBar = new JToolBar();
    breadcrumbToolBar.setOpaque(true);

    breadcrumbPanel = new JPanel();
    goUpIcon = new JLabel();
    goUpIcon.setVisible(false);
    // Design the toolbar.
    breadcrumbToolBar.setFloatable(false);
    breadcrumbToolBar.setRollover(true);

    // Add listener to the toolbar.
    breadcrumbToolBar.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        doBreadcrumbsLayout();
      }
    });

    // Set up the icon.
    goUpIcon.setOpaque(true);
    // Add the tooltip.
    goUpIcon.setToolTipText(GO_BACK_TOOLTIP);

    // Set cursor
    goUpIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    /*
     * Add listener for going only backwards using the logic from customJButton
     * component.
     */
    goUpIcon.addMouseListener(new GoUpHandler());
    // Set layout
    setLayout(new BorderLayout());
    breadcrumbPanel.setLayout(new GridBagLayout());

    // Set constraints
    GridBagConstraints c = new GridBagConstraints();

    // GoUpIcon
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(1, 10, 1, 5);
    breadcrumbPanel.add(goUpIcon, c);

    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1.0;
    c.insets = new Insets(1, 5, 1, 10);
    c.fill = GridBagConstraints.HORIZONTAL;

    breadcrumbPanel.add(breadcrumbToolBar, c);
    add(breadcrumbPanel, BorderLayout.CENTER);
  }

  /**
   * Creates a widget to put in the breadcrumbs toolbar.
   * 
   * @param resource
   * @return The button customized for the breadcrumb
   */
  public JButton createBreadcrumbButton(final IResource resource) {

    // Action is the listener of of button event
    Action breadcrumbAction = new BreadcrumbButtonAction(resource, parentResources, hiddenItems, breadcrumbToolBar,
        itemsPresenter);

    // Attach the event created
    return new ToolbarButton(breadcrumbAction, true);
  }

  /**
   * Gets the current folder.
   * 
   * @return
   */
  public FolderImpl getCurrentFolder() {
    return currentFolder;
  }

  /**
   * When there isn't enough room for all ancestors we will present them
   * 
   * @return An widget to present ancestors.
   */
  public JButton createCollapsedAncestorsWidget() {

    // Create the event of the button
    Action action = new AbstractAction("..") {

      @Override
      public void actionPerformed(ActionEvent e) {

        // Create the menu of the popUp button
        JPopupMenu popUpMenu = new JPopupMenu();

        // Iterate over all elements and attach the action of each
        for (JButton jButton : hiddenItems) {

          JMenuItem menuItem = new JMenuItem(jButton.getAction());

          // Add always the popUpButton
          popUpMenu.add(menuItem, 0);
        }

        // Show the popUpMenu
        Component source = (Component) e.getSource();
        popUpMenu.show(source, 0, source.getHeight());

      }
    };
    // Add event to the button
    return new ToolbarButton(action, true);
  }

  /**
   * Add a breadcrumb for this resource.
   * 
   * @param resource
   *          The resource to be added to the breadcrumb.
   */
  @Override
  public void addBreadcrumb(IResource resource) {

    // Set up the icon
    goUpIcon.setIcon(UIManager.getIcon("FileChooser.upFolderIcon"));

    // Set currentFolder
    currentFolder = (FolderImpl) resource;

    // Push to the parents stack
    parentResources.push(resource);

    if (logger.isDebugEnabled()) {
      logger.debug("Go to breadcrumb: " + parentResources.peek().getDisplayName());
    }

    // Add to the toolBar the popUp.
    // ----------------------------
    JButton breadcrumbButton = createBreadcrumbButton(resource);

    breadcrumbToolBar.add(breadcrumbButton);

    // Revalidates to not show an empty component.
    getParent().revalidate();
    getParent().repaint();

    // Invoke later to make sure the component is added
    // and has a dimension.
    SwingUtilities.invokeLater(() -> {
      // Check the if has necessary width
      boolean hasEnoughWidth = hasEnoughWidth(breadcrumbButton);
      if (!hasEnoughWidth) {
        // Refresh breadcrumb layout
        doBreadcrumbsLayout();

      }
    });
    // ----------------------------
  }

  /**
   * Check of can fit an additional breadcrumb in the toolbar.
   * 
   * @return <code>true</code> if there is enough space.
   */
  private boolean hasEnoughWidth(JButton breadcrumbButton) {
    return (getBreadcrumbsWidth(breadcrumbToolBar) + breadcrumbButton.getPreferredSize().getWidth()) < breadcrumbToolBar
        .getWidth();
  }

  /*
   * Reset the whole breadcrumb and data from it.
   */
  @Override
  public void resetBreadcrumb() {
    // Remove old data.
    goUpIcon.setVisible(false);
    parentResources.clear();
    hiddenItems.clear();
    breadcrumbToolBar.removeAll();

    // Revalidate to not show an empty component.
    getParent().revalidate();
    getParent().repaint();
  }

  /**
   * Computes the width of the breadcrumbs presented in the toolbar.
   * 
   * @param toolbar
   * 
   * @return value in pixels
   */

  private static int getBreadcrumbsWidth(JToolBar toolbar) {
    int totalWidth = 0;
    Component[] components = toolbar.getComponents();
    for (int i = 0; i < components.length; i++) {
      totalWidth += components[i].getWidth();
    }
    return totalWidth;
  }

  /*
   * Makes sure that items are either pushed to the hiddenItems stack or pushed
   * back to the breadcrumb if they fit the toolBar at that moment.
   */
  private void doBreadcrumbsLayout() {
    // Check if there is a widget
    boolean existsMoreWidget = false;
    if (breadcrumbToolBar.getComponentCount() > 0) {
      goUpIcon.setVisible(true);
      JButton first = (JButton) breadcrumbToolBar.getComponent(0);

      if (first.getText().equals("..")) {
        existsMoreWidget = true;
      }
    }

    // Push to the hiddenItems stack if the components do not fit the toolBar
    // and remove from toolBar.
    int counter = existsMoreWidget ? 1 : 0;
    while (getBreadcrumbsWidth(breadcrumbToolBar) > breadcrumbToolBar.getWidth()) {

      hiddenItems.push((JButton) breadcrumbToolBar.getComponentAtIndex(counter));
      breadcrumbToolBar.remove(counter);

    }

    // Push to the toolBar and remove from hiddenItems stack till they fit the
    // toolBar
    int componentsWidth = getBreadcrumbsWidth(breadcrumbToolBar);
    while (componentsWidth < breadcrumbToolBar.getWidth() && !hiddenItems.isEmpty()) {

      JButton item = hiddenItems.peek();
      double childWidth = item.getPreferredSize().getWidth();

      // Add the child width
      componentsWidth += childWidth;

      if (componentsWidth < breadcrumbToolBar.getWidth()) {
        breadcrumbToolBar.add(item, counter);

        hiddenItems.pop();
      }
    }

    // Add the "more items" widget if needed.
    if (!hiddenItems.isEmpty() && !existsMoreWidget) {

      // This will break the UI
      breadcrumbToolBar.add(createCollapsedAncestorsWidget(), 0);
    } else if (hiddenItems.isEmpty() && existsMoreWidget) {
      breadcrumbToolBar.remove(0);
    }

    // Revalidate to not show an empty component and refresh
    getParent().revalidate();
    getParent().repaint();
  }

  @Override
  public void repositoryConnected(URL serverURL, String repositoryID) {
    resetBreadcrumb();
  }

  /**
   * Handles go up requests triggered via the mouse.
   */
  private final class GoUpHandler extends MouseAdapter {
    @Override
    public void mouseClicked(final MouseEvent e) {
      // While goes back to the target selected pop elements
      // Remove descendants from the visible toolbar.

      // For leaving the rootFolder in place
      if (!parentResources.isEmpty()) {

        // For leaving the rootFolder in place
        if (breadcrumbToolBar.getComponentCount() > 1) {
          breadcrumbToolBar.remove(breadcrumbToolBar.getComponentCount() - 1);
          parentResources.pop();

        }
        // If toolBar does not have items and remove items from stacks till
        // there are hiddenItems left.
        if (breadcrumbToolBar.getComponentCount() == 0 && !hiddenItems.isEmpty()) {

          while (hiddenItems.peek() != null) {
            // Pop until we reach the target
            parentResources.pop();
            hiddenItems.pop();
          }
        }

        // Present the resources (children) of the items.
        if (!parentResources.isEmpty()) {
          itemsPresenter.presentResources(parentResources.peek().getId());
        }

        // Revalidates toolBar view and refresh
        breadcrumbToolBar.revalidate();
        breadcrumbToolBar.repaint();

        // Refresh the layout
        doBreadcrumbsLayout();
      }
    }
  }
}
