package com.oxygenxml.cmis.core.model.impl;

import java.util.Collections;
import java.util.Iterator;
import org.apache.chemistry.opencmis.client.api.Repository;

import com.oxygenxml.cmis.core.model.IRepository;
import com.oxygenxml.cmis.core.model.IResource;

public class RepositoryImpl implements IRepository {

  private Repository repository;

  public RepositoryImpl(Repository repository) {
    this.repository = repository;
  }

  @Override
  public Iterator<IResource> iterator() {

    return Collections.emptyIterator();
  }

  @Override
  public String getDisplayName() {

    return repository.getName();
  }

  @Override
  public String getId() {

    return repository.getId();
  }

  @Override
  public String getCreatedBy() {

    return null;
  }

  @Override
  public boolean isCheckedOut() {
    return false;
  }

  @Override
  public void refresh() {
    //Not implemented
    
  }

  @Override
  public String getDescription() {
    return repository.getDescription();
  }
}
