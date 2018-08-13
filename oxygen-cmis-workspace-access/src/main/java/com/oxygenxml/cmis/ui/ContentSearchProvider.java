package com.oxygenxml.cmis.ui;

import com.oxygenxml.cmis.core.model.IResource;

public interface ContentSearchProvider {

  String getLineDoc(IResource doc, String matchPattern);
}
