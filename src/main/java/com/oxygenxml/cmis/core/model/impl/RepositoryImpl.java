package com.oxygenxml.cmis.core.model.impl;

import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IRepository;
import com.oxygenxml.cmis.core.model.IResource;

public class RepositoryImpl implements IRepository {

  private Repository repository;
  
  public RepositoryImpl(Repository repository) {
    this.repository = repository;
  }
  
  @Override
  public Iterator<IResource> iterator() {
    // TODO Auto-generated method stub
    return Collections.emptyIterator();
  }

  @Override
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return repository.getName();
  }

  @Override
  public String getId() {
    // TODO Auto-generated method stub
    return repository.getId();
  }
 
  
}
