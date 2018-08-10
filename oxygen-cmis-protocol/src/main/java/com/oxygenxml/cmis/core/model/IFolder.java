package com.oxygenxml.cmis.core.model;

public interface IFolder extends IResource {

  String getFolderPath();

  void refresh();
}
