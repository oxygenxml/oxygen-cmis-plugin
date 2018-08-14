package com.oxygenxml.cmis.ui;

import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;

public interface ContentSearchProvider {

  String getLineDoc(IResource doc, String matchPattern);
  String getPath(IResource doc,ResourceController ctrl);
}
