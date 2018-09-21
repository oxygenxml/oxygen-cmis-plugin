package com.oxygenxml.cmis.ui;

import java.net.URL;

/**
 * Handles the repository's listenerrs
 * 
 * @author bluecc
 *
 */
public interface RepositoryListener {
  /**
   * Presents the repository and resets the breadcrumb
   * 
   * @param serverURL
   * @param repositoryID
   */
  void repositoryConnected(URL serverURL, String repositoryID);
}
