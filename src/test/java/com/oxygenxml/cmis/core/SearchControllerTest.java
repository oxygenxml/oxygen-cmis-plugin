package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
  public void testSearhcFiles() {
    SearchController search = new SearchController(ctrl);
    search.searchFiles("My_Document-0-1");
    
    ArrayList<IDocument> docs = search.resultDocs();
    
    assertNotNull(docs);

    for(IDocument doc : docs) {
      System.out.println(doc.getDisplayName());
    }
    
  }

}
