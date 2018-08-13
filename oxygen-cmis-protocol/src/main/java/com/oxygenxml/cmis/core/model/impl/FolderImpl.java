package com.oxygenxml.cmis.core.model.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;

public class FolderImpl implements IFolder {

  /**
   * Logger for logging.
   */
  private static Logger logger = Logger.getLogger(FolderImpl.class.getName());

  /**
   * Wrapped CMIS folder.
   */
  private Folder folder;

  public FolderImpl(Folder folder) {
    this.folder = folder;

  }

  public Folder getFolder() {
    return folder;
  }

  @Override
  public Iterator<IResource> iterator() {
    // TODO Alexey Teste JUNIT.
    return new ResourceIterator(folder);
  }

  private class ResourceIterator implements Iterator<IResource> {
    private Iterator<CmisObject> children;

    public ResourceIterator(Folder folder) {
      children = folder.getChildren().iterator();
    }

    @Override
    public boolean hasNext() {
      return children.hasNext();
    }

    @Override
    public IResource next() {
      CmisObject next = children.next();
      if (next instanceof Document) {
        return new DocumentImpl((Document) next);
      } else if (next instanceof Folder) {
        return new FolderImpl((Folder) next);
      } else {
        logger.error("Unhandled type " + next.getClass());
        return new OtherResource(next);
      }
    }
  }

  @Override
  public String getDisplayName() {
    return folder.getName();
  }

  @Override
  public String getId() {
    return folder.getId();
  }

  public String getFolderPath() {
    return folder.getPath();
  }

  public String getCreatedBy() {
    return folder.getCreatedBy();
  }

  public Date getTimeCreated() {
    return folder.getCreationDate().getTime();
  }

  public ItemIterable<QueryResult> getQuery(ResourceController ctrl) {
    String query = "SELECT * FROM cmis:folder WHERE cmis:name LIKE '".concat(getDisplayName()).concat("'");
    return ctrl.getSession().query(query, false);
  }

  /**
   * Another type of resource.
   */
  private class OtherResource implements IResource {
    /**
     * Wrapped CMIS object.
     */
    private CmisObject object;

    /**
     * Constructor.
     * 
     * @param object
     *          The wrapped CMIS object.
     */
    public OtherResource(CmisObject object) {
      this.object = object;
    }

    @Override
    public Iterator<IResource> iterator() {
      return Collections.emptyIterator();
    }

    @Override
    public String getDisplayName() {
      return object.getName() + " [" + object.getType() + "]";
    }

    @Override
    public String getId() {
      return object.getId();
    }

    @Override
    public String getCreatedBy() {

      return object.getCreatedBy();
    }

    @Override
    public boolean isCheckedOut() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void refresh() {
      object.refresh();
    }
  }

  @Override
  public boolean isCheckedOut() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void refresh() {
    folder.refresh();
  }
}
