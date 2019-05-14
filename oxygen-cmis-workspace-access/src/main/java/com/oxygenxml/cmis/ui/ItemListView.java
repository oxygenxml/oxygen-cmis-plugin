package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.CmisAccessSingleton;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.CmisURL;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

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
   * Breadcrumb presenter.
   */
  private final BreadcrumbPresenter breadcrumbPresenter;

  /**
   * Constructor that gets the tabPresenter to show documents in tabs and
   * breacrumbPrsenter to update the breadcrumb
   * 
   * @param tabsPresenter
   * @param breadcrumbPresenter
   */
  public ItemListView(BreadcrumbPresenter breadcrumbPresenter) {
    this.breadcrumbPresenter = breadcrumbPresenter;
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
    resourceList.addMouseListener(new ResourceMouseHandler(
        breadcrumbPresenter, 
        () -> resourceList, 
        () -> currentParent, 
        this));

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
    if (logger.isDebugEnabled()) {
      logger.debug("Present resources from server: " + connectionInfo + ", repository id: " + repositoryID);
    }
    try {
      connectToRepository(connectionInfo, repositoryID, CmisAccessSingleton.getInstance());
      loadRepositoryRoot();
      
    } catch (UserCanceledException e1) {
      // The user canceled the process.
      logger.error("Error ", e1);
    } catch (CmisRuntimeException e) {
      // Unexpected exception
      logger.debug(e, e);
      
      PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage("Unable to retrieve repositories because of: " + e.getMessage(), e);
    } 
  }

  private void loadRepositoryRoot() {
    // Get the instance
    CMISAccess instance = CmisAccessSingleton.getInstance();
    // Get the rootFolder and set the model
    Folder rootFolder = instance.createResourceController().getRootFolder();
    
    if (logger.isDebugEnabled()) {
      logger.debug("Root folder " + rootFolder);
    }

    final FolderImpl origin = new FolderImpl(rootFolder);
    
    DefaultListModel<IResource> model = new DefaultListModel<>();

    installDefaultRenderer();
    model.addElement(origin);
    resourceList.setModel(model);
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
      uc = AuthenticatorUtil.getUserCredentials(connectionInfo);

      if (logger.isDebugEnabled()) {
        logger.debug("Connect to url: " + connectionInfo + " repoID: " + repositoryID + " credentials: " + uc);
      }

      try {
        // Try to connect to the repository
        instance.connectToRepo(connectionInfo, repositoryID, uc);
        connected = true;

      } catch (CmisUnauthorizedException e) {
        // Will try again.
        if (logger.isDebugEnabled()) {
          logger.debug(e, e);
        }
      }

    } while (!connected);
  }

  /**
   * Presents all the children of the resource inside the list.
   * 
   * @param resource
   *          the resource to present its children.
   */
  @Override
  public void presentResources(IResource parentResource) {
    beforeSearchParent = null;
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

    if (logger.isDebugEnabled()) {
      logger.debug("Present children of resource: " + parentResource.getDisplayName());
    }
    // Get all the children of the item in an iterator
    final Iterator<IResource> childrenIterator = parentResource.iterator();
    
    // Iterate them till it has a child
    if (childrenIterator != null) {

      // While has a child, add to the model
      List<IResource> resources = new LinkedList<>();
      while (childrenIterator.hasNext()) {
        IResource iResource = childrenIterator.next();
        if (logger.isDebugEnabled()) {
          logger.debug("  Child " + iResource.getDisplayName() + " id " + iResource.getId());
        }

        // Only if it's not a PWC document add to the model
        boolean notPWC = !(
            iResource instanceof DocumentImpl 
            && ((DocumentImpl) iResource).isCheckedOut()
            && ((DocumentImpl) iResource).isPrivateWorkingCopy());
        
        if (notPWC) {
          // Private Working Copies are not presented. The user works with the original file transparently.
          // The application knows how to interpret the PWC.
          resources.add(iResource);
        }
      }
      
      Collections.sort(resources, (a, b) -> {
        // Folders should go first.
        int aPriority = a instanceof IFolder ? 0 : 1;
        int bPriority = b instanceof IFolder ? 0 : 1;
        int folderPriority = aPriority - bPriority;
        return 
            // If one is a folder, that one should go first.
            folderPriority != 0 ? folderPriority :
            // If both are folders or both are documents, compare their named.
            a.getDisplayName().compareTo(b.getDisplayName()); 
      });
      
      // Define a model for the list in order to render the items
      final DefaultListModel<IResource> model = new DefaultListModel<>();
      for (IResource iResource : resources) {
        model.addElement(iResource);
      }

      // Set the model to the list
      resourceList.setModel(model);
    }
  }

  @Override
  public void presentResources(String folderID) {
    installDefaultRenderer();
    
    ResourceController resourceController = CmisAccessSingleton.getInstance().createResourceController();

    // Present the folder children
    Folder folder = resourceController.getFolder(folderID);
    boolean isCurrentParent = folderID.equals(currentParent.getId());
    LinkedList<Folder> ancestors = new LinkedList<>();
    if (!isCurrentParent) {
      ancestors.addFirst(folder);
      // Update breadcrumb. Collect all ancestors.
      Folder p = folder.getFolderParent();
      while (p != null) {
        ancestors.addFirst(p);
        p = p.getFolderParent();
      }
      
      breadcrumbPresenter.resetBreadcrumb();
      for (Folder ancestor : ancestors) {
        breadcrumbPresenter.addBreadcrumb(new FolderImpl(ancestor));
      }
     }
    presentResources(new FolderImpl(folder));
  }

  private void installDefaultRenderer() {
    resourceList.setCellRenderer(new DefaultListCellRendererExtension());
  }

  private IResource beforeSearchParent = null;

  @Override
  public void searchFinished(String filter, final List<IResource> resources, String option, boolean searchFolders) {
    if (filter != null && filter.length() > 0) {
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
          contentProvider.doSearch(filter, searchFolders);
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
      
      if (currentParent != null 
          && !SEARCH_RESULTS_ID.equals(currentParent.getId())) {
        // Keep the parent before a search event so we can restore it.
        beforeSearchParent = currentParent;
      }

      presentResourcesInternal(parentResource);
    } else if (beforeSearchParent != null) {
      presentResources(beforeSearchParent);
      beforeSearchParent = null;
    } else {
      loadRepositoryRoot();
    }
  }

  @Override
  public void repositoryConnected(URL serverURL, String repositoryID) {
    presentResources(serverURL, repositoryID);
  }

  /**
   * @return The selected CMIS object.
   */
  public CmisObject getSelectedCmisObject() {
    CmisObject object = null;
    IResource selectedResource = resourceList.getSelectedValue();

    if (selectedResource == null) {
      // Consider the parent resource as the selected resource.
      selectedResource = currentParent;
    }
    
    if (selectedResource != null) {
      if (selectedResource instanceof DocumentImpl) {
        object = ((DocumentImpl) selectedResource).getDoc();

      } else if (selectedResource instanceof FolderImpl) {
        object = ((FolderImpl) selectedResource).getFolder();

      }
    }
    
    return object;
  }

  /**
   * Refresh the presented resources if needed.
   * 
   * @param savedURL A new resource that was changed.
   */
  public void refresh(URL savedURL) {
    try {
      if (savedURL.getProtocol().equals(CmisURL.CMIS_PROTOCOL)) {
        // The given URL points to a CMIS resource.
        CmisURLConnection connection = (CmisURLConnection) savedURL.openConnection();
        CmisObject cmisObject = connection.getCMISObject(savedURL.toString());

        if (logger.isDebugEnabled()) {
          logger.debug("refresh for " + savedURL);
        }
        logger.info("refresh for " + savedURL);
        if (cmisObject instanceof FileableCmisObject) {
          String currentFolderPath = ((IFolder) currentParent).getFolderPath();
          if (logger.isDebugEnabled()) {
            logger.debug("Current path: " + currentFolderPath);
          }
          logger.info("Current path: " + currentFolderPath);
          
          List<Folder> parents = ((FileableCmisObject) cmisObject).getParents();
          for (Folder folder : parents) {
            if (logger.isDebugEnabled()) {
              logger.debug("parent path " + folder.getPath());
            }
            logger.info("parent path " + folder.getPath());
            
            if (folder.getPath().equals(currentFolderPath)
                &&!isResourcePresentInModel(cmisObject)) {
              logger.info("Do it");
              // Refresh only if the resource is not already presented.
              presentResources(currentParent);
               break;
            }
          }
        }
      }
    } catch (IOException e) {
      logger.error(e, e);
    }
  }

  /**
   * Checks if the given resource is already presented in the list model.
   * 
   * @param cmisObject Resource to check if it exists.
   * 
   * @return true if the resource is already present in tyhe model.
   */
  private boolean isResourcePresentInModel(CmisObject cmisObject) {
    boolean found = false;
    ListModel<IResource> model = resourceList.getModel();
    int size = model.getSize();
    for (int i = 0; i < size; i++) {
      IResource elementAt = model.getElementAt(i);
      if (elementAt.getId().equals(cmisObject.getId())) {
        found = true;
        break;
      }
    }
    return found;
  }
}
