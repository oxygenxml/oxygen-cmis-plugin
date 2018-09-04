package com.oxygenxml.cmis.core.model;

import org.apache.chemistry.opencmis.client.api.Document;

public interface IFolder extends IResource {

  String getFolderPath();

  void addToModel(Document doc);

  void removeFromModel(IResource resource);

  void refresh();
}
