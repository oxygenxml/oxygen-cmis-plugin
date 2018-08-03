package com.oxygenxml.cmis.ui;

import java.net.URL;

/**
 * Make sure to present the repositories when needed and hide implementation
 * 
 * @author bluecc
 *
 */
public interface RepositoriesPresenter {
  /**
   * Present the repositories tih the url entered
   * 
   * @param serverURL
   */
  void presentRepositories(URL serverURL);
}
