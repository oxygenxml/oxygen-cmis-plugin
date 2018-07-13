package com.oxygenxml.cmis.ui;

import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Repository;

import com.oxygenxml.cmis.core.model.IResource;

public interface ItemsPresenter {
  void presentItems(URL connectionInfo, String repositoryID);
 
}
