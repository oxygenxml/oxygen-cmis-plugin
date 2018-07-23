package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IFolder;



public class SearchControllerTest extends ConnectionTestBase{
  
  private ResourceController ctrl;
  
  @Before
  public void setUp() throws MalformedURLException {
    CMISAccess.getInstance().connect(new URL("http://localhost:8080/B/atom11"), "A1");
    ctrl = CMISAccess.getInstance().createResourceController();
  }
  
  @Test
  public void testQueringFolders() {
    SearchController search = new SearchController(ctrl);
    
    ArrayList<IFolder> folds = search.queringFolder("Fold");
    
    assertNotNull(folds);

    for(IFolder folder : folds) {
      System.out.println(folder.getDisplayName());
    }
  }
  
  @Test
  public void testQueringDoc() {
    SearchController search = new SearchController(ctrl);
    
    ArrayList<IDocument> docs = search.queringDoc("Document");
    
    assertNotNull(docs);

    for(IDocument doc : docs) {
      System.out.println(doc.getDisplayName());
    }
  }
  
  @Test
  public void testQueringDocContent() {
    SearchController search = new SearchController(ctrl);
    
    ArrayList<IDocument> docs = search.queringDocContent("At justo in urna");
    
    assertNotNull(docs);

    for(IDocument doc : docs) {
      System.out.println(doc.getDisplayName());
    }
    
  }

  @After
  public void afterMethod(){
    ctrl.getSession().clear();
  }
}
