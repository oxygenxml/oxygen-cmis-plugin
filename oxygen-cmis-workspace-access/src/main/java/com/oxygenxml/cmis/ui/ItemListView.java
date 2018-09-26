package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

/**
 * Describes how the folders and documents are: displayed, rendered, their
 * actions.
 * 
 * 
 * @author bluecc
 *
 */
public class ItemListView extends JPanel implements ResourcesBrowser, SearchListener, RepositoryListener {

  private static final String SEARCH_RESULTS_VALUE = "Search results";

  private static final String SEARCH_RESULTS_ID = "#search.results";

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
  private transient ContentSearcher contentProvider;

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
    resourceList.addMouseListener(new ResourceMouseHandler(breadcrumbPresenter, () -> {

      return resourceList;
    }, () -> {

      return currentParent;
    }, this));

    // Set layout
    setLayout(new BorderLayout(0, 0));

    add(listItemScrollPane, BorderLayout.CENTER);

  }

  /**
   * Sets the data to be used
   * 
   * @param contentProvider
   */
  public void setContentProvider(ContentSearcher contentProvider) {
    this.contentProvider = contentProvider;
    contentProvider.addSearchListener(this);
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
      logger.error("Error ", e1);

    }
  }

  /**
   * Checks the connection to the server. If the method returns without
   * exception, the connection was successful.
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

    logger.debug("Current item=" + parentResource.getDisplayName());
    // Get all the children of the item in an iterator
    final Iterator<IResource> childrenIterator = parentResource.iterator();

    // Iterate them till it has a child
    if (childrenIterator != null) {

      // Define a model for the list in order to render the items
      final DefaultListModel<IResource> model = new DefaultListModel<>();

      // While has a child, add to the model
      while (childrenIterator.hasNext()) {
        IResource iResource = childrenIterator.next();

        // Only if it's not a PWC document add to the model
        boolean notPWC = !(iResource instanceof DocumentImpl && ((DocumentImpl) iResource).isCheckedOut()
            && ((DocumentImpl) iResource).isPrivateWorkingCopy());

        if (notPWC) {
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
        return SEARCH_RESULTS_ID;
      }

      @Override
      public String getDisplayName() {
        return SEARCH_RESULTS_VALUE;
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

  @Override
  public void repositoryConnected(URL serverURL, String repositoryID) {
    presentResources(serverURL, repositoryID);
  }

  public String getSelectedObjectUrl() {
    CmisObject object = null;
    IResource selectedResource = resourceList.getSelectedValue();

    logger.info("Reaches present SelectedURL?");
    if (selectedResource != null) {
      logger.info("Selected resource exists");
      if (selectedResource instanceof DocumentImpl) {
        object = ((DocumentImpl) selectedResource).getDoc();

      } else if (selectedResource instanceof FolderImpl) {
        object = ((FolderImpl) selectedResource).getFolder();

      }
    } else {
      logger.info("Selected resource is null");
    }
    
    ResourceController resourceController = CMISAccess.getInstance().createResourceController();
    logger.info("Controller " + resourceController );
    if (resourceController != null) {
      logger.info("Sess " + resourceController.getSession());
      
      if (resourceController.getSession() != null) {
        logger.info("params " + resourceController.getSession().getSessionParameters());
      }
    }
    
    return object != null ? CmisURLConnection.generateURLObject(object, resourceController) : null;
  }
}
