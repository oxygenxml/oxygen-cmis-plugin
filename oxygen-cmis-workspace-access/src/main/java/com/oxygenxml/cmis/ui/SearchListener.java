package com.oxygenxml.cmis.ui;
import java.util.List;

import com.oxygenxml.cmis.core.model.IResource;

public interface SearchListener {
  
  void searchFinished(String filter, List <IResource> resources);
  
}
