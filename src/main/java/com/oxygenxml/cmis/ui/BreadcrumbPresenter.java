package com.oxygenxml.cmis.ui;

import com.oxygenxml.cmis.core.model.IResource;

public interface BreadcrumbPresenter {
  
  void presentBreadcrumb(IResource resource);
  void resetBreadcrumb(boolean flag);
}
