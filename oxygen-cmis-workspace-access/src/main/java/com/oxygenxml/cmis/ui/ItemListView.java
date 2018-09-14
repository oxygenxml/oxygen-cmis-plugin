package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

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
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes how the folders and documents are: displayed, rendered, their
 * actions.
 * 
 * 
 * @author bluecc
 *
 */
public class ItemListView extends JPanel implements ResourcesBrowser, SearchListener, RepositoryListener {

  private static final String SEARCH_RESULTS = "#search.results";

  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(ItemListView.class);

  /**
   * The resource for which we currently present its children.
   */
  private transient IResource currentParent;
  /**
   * All the children to present.
   */
  private final JList<IResource> resourceList;

  private static final int COPY_PERMISSIONS = TransferHandler.MOVE;
  /**
   * Search support.
   */
  private ContentSearcher contentProvider;

  /**
   * Constructor that gets the tabPresenter to show documents in tabs and
   * breacrumbPrsenter to update the breadcrumb
   * 
   * @param tabsPresenter
   * @param breadcrumbPresenter
   */
  public ItemListView(TabsPresenter tabsPresenter, BreadcrumbPresenter breadcrumbPresenter) {

    // Create the listItem
    resourceList = new JList<>();
    resourceList.setSelectedIndex(0);
    resourceList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Scroller for the listRepo
    final JScrollPane listItemScrollPane = new JScrollPane(resourceList);

    /*
     * Drag and drop move item
     */
    resourceList.setDragEnabled(true);
    resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    resourceList.setTransferHandler(new ToTransferHandler(resourceList, COPY_PERMISSIONS));
    resourceList.setDropMode(DropMode.ON_OR_INSERT);

    /*
     * Render all the elements of the listItem when necessary
     * 
     * @return Component
     * 
     * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
     * 
     * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
     */
    resourceList.setCellRenderer(new DefaultListCellRendererExtension());
    /*
     * Add listener to the entire list
     * 
     * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
     * 
     * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
     */
    resourceList.addMouseListener(new ResourceMouseHandler(breadcrumbPresenter));

    // Set layout
    setLayout(new BorderLayout(0, 0));

    add(listItemScrollPane, BorderLayout.CENTER);

  }

  public void setContentProvider(ContentSearcher contentProvider) {
    this.contentProvider = contentProvider;
    contentProvider.addSearchListener(this);
  }

  /**
   * Populates the contextual menu with generic actions. Used when nothing is
   * selected in the list.
   */
  private void addGenericActions(JPopupMenu menu) {
    // Create a document in the current folder
    menu.add(new CreateDocumentAction(currentParent, this));
    // Create a folder in the current folder
    menu.add(new CreateFolderAction(currentParent, this));
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
    menu.add(new OpenDocumentAction(selectedResource));
    menu.add(new RenameDocumentAction(selectedResource, currentParent, this));
    menu.add(new CopyDocumentAction(selectedResource));
    menu.add(new DeleteDocumentAction(selectedResource, currentParent, this));
    menu.add(new CheckinDocumentAction(selectedResource, currentParent, this));
    menu.add(new CheckoutDocumentAction(selectedResource, currentParent, this));
    menu.add(new CancelCheckoutDocumentAction(selectedResource, currentParent, this));

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
    menu.add(new CreateDocumentAction(selectedResource, this));
    // Create a folder in the current folder
    menu.add(new CreateFolderAction(selectedResource, this));

    menu.add(new RenameFolderAction(selectedResource, currentParent, this));
    // TODO Cristian copy all resources postponed
    menu.add(new CopyFolderAction(selectedResource));

    menu.add(new PasteDocumentAction(selectedResource, currentParent, this));
    menu.add(new DeleteFolderAction(selectedResource, currentParent, this));

    menu.add(new CheckinFolderAction(selectedResource, currentParent, this));
    menu.add(new CheckoutFolderAction(selectedResource, currentParent, this));
    menu.add(new CancelCheckoutFolderAction(selectedResource, currentParent, this));
  }

  /**
   * Implemented presentItems using connectionInfo and repoID !! Model shall be
   * created whenever the list is updated Facade Pattern
   */
  @Override
  public void presentResources(URL connectionInfo, String repositoryID) {
    try {
      // Get the instance
      CMISAccess instance = CMISAccess.getInstance();

      connectToRepository(connectionInfo, repositoryID, instance);
      // Get the rootFolder and set the model
      Folder rootFolder = instance.createResourceController().getRootFolder();

      final FolderImpl origin = new FolderImpl(rootFolder);
      setFolder(origin);
    } catch (UserCanceledException e1) {
      // The user canceled the process.
      logger.debug(e1, e1);

      // Show the exception if there is one
      // TODO Cristian Pass a parent.
      JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
    }
  }

  /**
   * Checks the connection to the server. If the method returns without
   * exception, the connection was successfull.
   * 
   * @param connectionInfo
   *          URL to the server.
   * @param repositoryID
   *          The ID
   * @param instance
   * 
   * @throws UserCanceledException
   *           Unable to connect and the
   */
  private void connectToRepository(URL connectionInfo, String repositoryID, CMISAccess instance)
      throws UserCanceledException {
    boolean connected = false;
    // Connect
    UserCredentials uc = null;
    do {
      try {

        // Try to connect to the repository
        instance.connectToRepo(connectionInfo, repositoryID, uc);
        connected = true;

      } catch (CmisUnauthorizedException e) {
        // Get the credentials and show login dialog if necessary
        uc = AuthenticatorUtil.getUserCredentials(connectionInfo);

        if (logger.isDebugEnabled()) {
          logger.debug("User credit item list" + uc.getUsername());
        }
      }

    } while (!connected);
  }

  /**
   * Set the root folder and use the model to be rendered
   * 
   * @param origin
   */
  private void setFolder(final FolderImpl origin) {
    DefaultListModel<IResource> model = new DefaultListModel<>();

    installDefaultRenderer();
    model.addElement(origin);
    resourceList.setModel(model);
  }

  /**
   * Presents all the children of the resource inside the list.
   * 
   * @param resource
   *          the resource to present its children.
   */
  @Override
  public void presentResources(IResource parentResource) {
    // Install a renderer
    installDefaultRenderer();

    presentResourcesInternal(parentResource);
  }

  /**
   * Present the resources inside UI
   * 
   * @param parentResource
   */
  private void presentResourcesInternal(IResource parentResource) {

    this.currentParent = parentResource;

    System.out.println("Current item=" + parentResource.getDisplayName());
    // Get all the children of the item in an iterator
    final Iterator<IResource> childrenIterator = parentResource.iterator();

    // Iterate them till it has a child
    if (childrenIterator != null) {

      // Define a model for the list in order to render the items
      final DefaultListModel<IResource> model = new DefaultListModel<>();

      // While has a child, add to the model
      while (childrenIterator.hasNext()) {
        IResource iResource = childrenIterator.next();
        // Only if it's not a locked document add to the model
        if (!(iResource instanceof DocumentImpl && ((DocumentImpl) iResource).isCheckedOut()
            && !((DocumentImpl) iResource).isPrivateWorkingCopy())) {

          model.addElement(iResource);
        }

      }

      // Set the model to the list
      resourceList.setModel(model);
    }
  }

  @Override
  public void presentResources(String folderID) {

    installDefaultRenderer();

    ResourceController resourceController = CMISAccess.getInstance().createResourceController();
    // Present the folder children
    presentResources(new FolderImpl(resourceController.getFolder(folderID)));
  }

  private void installDefaultRenderer() {
    resourceList.setCellRenderer(new DefaultListCellRendererExtension());
  }

  @Override
  public void searchFinished(String filter, final List<IResource> resources) {

    // Provides the threads needed for async response
    final CacheSearchProvider csp = new CacheSearchProvider(contentProvider, resourceList);

    // Create a rendered by using the custom renderer with the resources from
    // cache (data gotten and the filter(text to search))
    SearchResultCellRenderer seachRenderer = new SearchResultCellRenderer(csp, filter);
    resourceList.setCellRenderer(seachRenderer);

    final IResource parentResource = new IFolder() {
      @Override
      public Iterator<IResource> iterator() {
        return resources.iterator();
      }

      @Override
      public boolean isCheckedOut() {
        return false;
      }

      @Override
      public String getId() {
        return SEARCH_RESULTS;
      }

      @Override
      public String getDisplayName() {
        return "Search results";
      }

      @Override
      public String getCreatedBy() {
        return null;
      }

      @Override
      public void refresh() {
        contentProvider.doSearch(filter);
      }

      @Override
      public String getFolderPath() {
        return null;
      }

      @Override
      public String getDescription() {
        return null;
      }

      @Override
      public void addToModel(Document doc) {
        // TODO: add
        ((DefaultListModel<IResource>) resourceList.getModel()).addElement(new DocumentImpl(doc));
      }

      @Override
      public void removeFromModel(IResource resource) {
        final int index = ((DefaultListModel<IResource>) resourceList.getModel()).indexOf(resource);
        ((DefaultListModel<IResource>) resourceList.getModel()).remove(index);

      }
    };

    presentResourcesInternal(parentResource);
  }

  /**
   * Mouse interaction support.
   */
  private final class ResourceMouseHandler extends MouseAdapter {
    private final BreadcrumbPresenter breadcrumbPresenter;

    public ResourceMouseHandler(BreadcrumbPresenter breadcrumbPresenter) {
      this.breadcrumbPresenter = breadcrumbPresenter;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
      // Get the location of the item using location of the click
      int itemIndex = resourceList.locationToIndex(e.getPoint());

      if (itemIndex != -1) {
        // Get the current item
        IResource currentItem = resourceList.getModel().getElementAt(itemIndex);

        // If right click was pressed
        if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
          JPopupMenu menu = new JPopupMenu();

          // Get the bounds of the item
          Rectangle cellBounds = resourceList.getCellBounds(itemIndex, itemIndex);

          // Check if the click was outside the visible list
          if (!cellBounds.contains(e.getPoint())) {
            // Check is has a parent folder for the creation
            if (currentParent != null && !currentParent.getId().equals(SEARCH_RESULTS)) {
              if (logger.isDebugEnabled()) {
                logger.debug("ID item = " + ((IFolder) currentParent).getId());
                logger.debug("Name item!!!! = " + currentParent.getDisplayName());
              }
              addGenericActions(menu);
            }
          } else {

            // Set selected on right click
            resourceList.setSelectedIndex(itemIndex);

            if (currentItem instanceof DocumentImpl) {

              // Create the JMenu for the document
              addDocumentActions(currentItem, menu);

            } else if (currentItem instanceof FolderImpl) {

              // Create the JMenu for the folder
              addFolderActions(currentItem, menu);

            }
          }

          // Bounds of the click
          menu.show(resourceList, e.getX(), e.getY());

        }

        // Check if we have a double click.
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
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
            presentResources(currentItem);
          }
        }
      }
    }
  }

  /**
   * Default renderer for resources.
   */
  private static final class DefaultListCellRendererExtension extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {

      Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (component instanceof JLabel) {
        String renderText = "";

        if (value != null) {
          JLabel comLabel = ((JLabel) component);
          IResource resource = ((IResource) value);

          // Cast in order to use the methods from IResource interface
          renderText = resource.getDisplayName();
          
          //All PWC has a space in the front of the string (Working Copy).
          comLabel.setText(renderText.replace(" (Working Copy)", "").trim());

          // If it's an instance of custom type of Folder.
          if ((IResource) value instanceof FolderImpl) {

            comLabel.setIcon(UIManager.getIcon("FileView.directoryIcon"));

          } else if (resource instanceof DocumentImpl) {
            DocumentImpl doc = ((DocumentImpl) resource);
            // Check if it's a Private Working Copy.
            if (doc.isCheckedOut() && doc.isPrivateWorkingCopy()) {

              comLabel.setIcon(new ImageIcon(getClass().getResource("/images/padlock.png")));

            } else {
              // ---------Use Oxygen Icons.
              try {
                comLabel.setIcon((Icon) PluginWorkspaceProvider.getPluginWorkspace().getImageUtilities()
                    .getIconDecoration(new URL("http://localhost/" + resource.getDisplayName())));

              } catch (final MalformedURLException e) {
                // If it's an instance of custom type of Folder.
                // Set the native icon to the component.
                comLabel.setIcon(UIManager.getIcon("FileView.fileIcon"));
                logger.error(e, e);

              }
              // ---------
            }
          }
        }
      }

      return component;
    }
  }

  @Override
  public void repositoryConnected(URL serverURL, String repositoryID) {
    presentResources(serverURL, repositoryID);
  }
}
