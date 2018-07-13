package com.oxygenxml.cmis.core.model;

import java.util.Iterator;

/**
 * TODO Alexey Test the implementations that they can actually iterate over the resources structure.
 *  
 * 
 * A CMIS resource.
 */
public interface IResource {
  /**
   * Gets an iterator over the children resources.
   * 
   * @return An iterator over the child resources or <code>null</code> for a leaf.
   */
  Iterator<IResource> iterator();
  
  String getDisplayName();
  
  String getId();

}
