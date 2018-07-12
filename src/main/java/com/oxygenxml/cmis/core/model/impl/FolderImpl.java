package com.oxygenxml.cmis.core.model.impl;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.log4j.Logger;

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
      }
      
      throw new NoSuchElementException();
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
}
