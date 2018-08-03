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
   * Presents the resource to the breadcrumb
   * 
   * @param resource
   * 
   * @see com.oxygenxml.cmis.core.model.IResource
   */
  void presentBreadcrumb(IResource resource);

  /**
   * Flag for reseeting the breadcrumb as it was initially
   * 
   * @param flag
   * 
   */
  void resetBreadcrumb(boolean flag);
}
