package com.oxygenxml.cmis.core.model;

import com.oxygenxml.cmis.core.ResourceController;

public interface IDocument extends IResource {
  
  String getDocumentPath(ResourceController ctrl);

}
