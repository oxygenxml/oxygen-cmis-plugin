package com.oxygenxml.cmis.ui;

import java.util.Iterator;
import java.util.Stack;

import com.oxygenxml.cmis.core.model.IResource;

class GoUpResource implements IResource {
  
  Stack<IResource> parentResources;
  
   GoUpResource(Stack<IResource> parentResources) {
    this.parentResources = parentResources;
  }
  
  @Override
  public Iterator<IResource> iterator() {
    IResource wrapped = parentResources.peek();
    return wrapped .iterator();
  }

  @Override
  public String getDisplayName() {
    return "..";
  }

  @Override
  public String getId() {
    return parentResources.peek().getId();
  }
}