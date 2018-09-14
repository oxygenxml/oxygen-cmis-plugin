package com.oxygenxml.cmis.ui;

import com.oxygenxml.cmis.core.model.IResource;

/**
 * Presenter that helps hiding the object adding to it some functionality
 * 
 * @author bluecc
 *
 */
public interface BreadcrumbPresenter {
  /**
   * Appends this resource to the resources from the breadcrumb view.
   * 
   * @param resource
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  void addBreadcrumb(IResource resource);

  /**
   * Clear all resources presented in the breadcrumb view. 
   */
  void resetBreadcrumb();
}
