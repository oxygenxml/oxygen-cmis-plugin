package com.oxygenxml.cmis.ui;

import java.net.URL;

public interface RepositoryListener {
  void repositoryConnected(URL serverURL, String repositoryID);
}
