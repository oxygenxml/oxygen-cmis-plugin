package com.oxygenxml.cmis.ui;

import java.net.URL;

import com.oxygenxml.cmis.core.model.IResource;

/**
 * Makes sure tht the items and the folders can be presented
 * 
 * @author bluecc
 */
public interface ResourcesBrowser {

  /**
   * Presents the resources inside the repository.
   * 
   * @param serverURL URL to the CMIS server.
   * @param repositoryID Repository ID.
   */
  void presentResources(URL serverURL, String repositoryID);

  /**
   * Presents the child resources.
   * 
   * @param resourceID Identifies the resource whos children will be presented.
   */
  void presentResources(String resourceID);

  /**
   * Presents the child resources.
   * 
   * @param resourceID Identifies the resource whos children will be presented.
   */
  void presentResources(IResource parentResource);

}
