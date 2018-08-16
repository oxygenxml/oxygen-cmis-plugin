package com.oxygenxml.cmis.ui;

import java.net.URL;

import com.oxygenxml.cmis.core.model.IFolder;
import com.oxygenxml.cmis.core.model.IResource;

/**
 * Makes sure tht the items and the folders can be presented
 * 
 * @author bluecc
 *
 */
public interface ItemsPresenter {

  /**
   * Present the items inside the folder
   * 
   * @param connectionInfo
   * @param repositoryID
   */
  void presentItems(URL connectionInfo, String repositoryID);

  /**
   * use the folder id to present the items inside
   * 
   * @param folderID
   */
  void presentFolderItems(String folderID);

  /**
   * use the folder id to present the items inside
   * 
   * @param folderID
   */
  void presentFolderItems(IFolder folder);

  void presentResources(IResource parentResource);

}
