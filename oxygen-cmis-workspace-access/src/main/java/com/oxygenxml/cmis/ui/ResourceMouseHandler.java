package com.oxygenxml.cmis.ui;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.google.common.base.Supplier;
import com.oxygenxml.cmis.actions.CancelCheckoutDocumentAction;
import com.oxygenxml.cmis.actions.CancelCheckoutFolderAction;
import com.oxygenxml.cmis.actions.CheckinDocumentAction;
import com.oxygenxml.cmis.actions.CheckinFolderAction;
import com.oxygenxml.cmis.actions.CheckoutDocumentAction;
import com.oxygenxml.cmis.actions.CheckoutFolderAction;
import com.oxygenxml.cmis.actions.CopyDocumentAction;
import com.oxygenxml.cmis.actions.CopyFolderAction;
import com.oxygenxml.cmis.actions.CreateDocumentAction;
import com.oxygenxml.cmis.actions.CreateFolderAction;
import com.oxygenxml.cmis.actions.DeleteDocumentAction;
import com.oxygenxml.cmis.actions.DeleteFolderAction;
import com.oxygenxml.cmis.actions.OpenDocumentAction;
import com.oxygenxml.cmis.actions.PasteDocumentAction;
import com.oxygenxml.cmis.actions.RenameDocumentAction;
import com.oxygenxml.cmis.actions.RenameFolderAction;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

/**
 * Mouse interaction support.
 */
class ResourceMouseHandler extends MouseAdapter {
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(ResourceMouseHandler.class);
  private static final String SEARCH_RESULTS = "#search.results";
  private final BreadcrumbPresenter breadcrumbPresenter;
  private final ResourcesBrowser itemsPresenter;
  private final Supplier<JList<IResource>> resourceListSupplier;
  private final Supplier<IResource> currentParentSupplier;

  public ResourceMouseHandler(BreadcrumbPresenter breadcrumbPresenter, Supplier<JList<IResource>> resourceListSupplier,
      Supplier<IResource> currentParentSupplier, ResourcesBrowser itemsPresenter) {
    this.breadcrumbPresenter = breadcrumbPresenter;
    this.resourceListSupplier = resourceListSupplier;
    this.currentParentSupplier = currentParentSupplier;
    this.itemsPresenter = itemsPresenter;
  }

  /**
   * Populates the contextual menu with generic actions. Used when nothing is
   * selected in the list.
   */
  private void addGenericActions(JPopupMenu menu) {
    // Create a document in the current folder
    menu.add(new CreateDocumentAction(currentParentSupplier.get(), itemsPresenter));
    // Create a folder in the current folder
    menu.add(new CreateFolderAction(currentParentSupplier.get(), itemsPresenter));
  }

  /**
   * Adds the actions that manipulate documents.
   * 
   * @param selectedResource
   *          The selected resource. The actions must manipulate it.
   * @param menu
   *          The menu to add the actions to.
   */
  private void addDocumentActions(final IResource selectedResource, JPopupMenu menu) {
    // CRUD Document
    IResource currentParent = currentParentSupplier.get();
    
    menu.add(new OpenDocumentAction(selectedResource));
    menu.add(new RenameDocumentAction(selectedResource, currentParent, itemsPresenter));
    menu.add(new CopyDocumentAction(selectedResource));
    menu.add(new DeleteDocumentAction(selectedResource, currentParent, itemsPresenter));
    menu.add(new CheckinDocumentAction(selectedResource, currentParent, itemsPresenter));
    menu.add(new CheckoutDocumentAction(selectedResource, currentParent, itemsPresenter));
    menu.add(new CancelCheckoutDocumentAction(selectedResource, currentParent, itemsPresenter));

    // TODO Cristian Check if is removed one reference or all maybe use of
    // removeFromFolder

    // TODO Cristian Drag and drop
    // Move to Folder
  }

  /**
   * Adds the actions that manipulate folders.
   * 
   * @param selectedResource
   *          The selected resource. The actions must manipulate it.
   * @param menu
   *          The menu to add the actions to.
   */
  private void addFolderActions(final IResource selectedResource, JPopupMenu menu) {
    // CRUD Folder
    IResource currentParent = currentParentSupplier.get();
    
    menu.add(new CreateDocumentAction(selectedResource, itemsPresenter));
    // Create a folder in the current folder
    menu.add(new CreateFolderAction(selectedResource, itemsPresenter));

    menu.add(new RenameFolderAction(selectedResource, currentParent, itemsPresenter));
    // TODO Cristian copy all resources postponed
    menu.add(new CopyFolderAction(selectedResource));

    menu.add(new PasteDocumentAction(selectedResource, currentParent, itemsPresenter));
    menu.add(new DeleteFolderAction(selectedResource, currentParent, itemsPresenter));

    menu.add(new CheckinFolderAction(selectedResource, currentParent, itemsPresenter));
    menu.add(new CheckoutFolderAction(selectedResource, currentParent, itemsPresenter));
    menu.add(new CancelCheckoutFolderAction(selectedResource, currentParent, itemsPresenter));
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    // Get the location of the item using location of the click
    int itemIndex = resourceListSupplier.get().locationToIndex(event.getPoint());

    if (itemIndex != -1) {
      // Get the current item
      IResource currentItem = resourceListSupplier.get().getModel().getElementAt(itemIndex);

      rightClick(event, currentItem, itemIndex);

      doubleLeftClick(event, currentItem);
    }
  }

  private void rightClick(final MouseEvent e, IResource currentItem, int itemIndex) {
    // If right click was pressed
    if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
      JPopupMenu menu = new JPopupMenu();

      // Get the bounds of the item
      Rectangle cellBounds = resourceListSupplier.get().getCellBounds(itemIndex, itemIndex);

      // Check if the click was outside the visible list
      if (!cellBounds.contains(e.getPoint())) {
        // Check is has a parent folder for the creation
        if (currentParentSupplier != null && !currentParentSupplier.get().getId().equals(SEARCH_RESULTS)) {
          if (logger.isDebugEnabled()) {
            logger.debug("ID item = " + ((IFolder) currentParentSupplier).getId());
            logger.debug("Name item!!!! = " + currentParentSupplier.get().getDisplayName());
          }
          addGenericActions(menu);
        }
      } else {

        // Set selected on right click
        resourceListSupplier.get().setSelectedIndex(itemIndex);

        if (currentItem instanceof DocumentImpl) {

          // Create the JMenu for the document
          addDocumentActions(currentItem, menu);

        } else if (currentItem instanceof FolderImpl) {

          // Create the JMenu for the folder
          addFolderActions(currentItem, menu);

        }
      }

      // Bounds of the click
      menu.show(resourceListSupplier.get(), e.getX(), e.getY());

    }
  }

  private void doubleLeftClick(final MouseEvent event, IResource currentItem) {
    // Check if we have a double click.
    if (event.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(event)) {
      if (logger.isDebugEnabled()) {
        logger.debug("TO present breadcrumb=" + currentItem.getDisplayName());
      }

      if (currentItem instanceof DocumentImpl) {
        // Open the document in Oxygen.
        new OpenDocumentAction(currentItem).openDocumentPath();

      } else {
        // Present the next item (folder)
        breadcrumbPresenter.addBreadcrumb(currentItem);
        // Present the folder children.
        itemsPresenter.presentResources(currentItem);
      }
    }
  }
}
