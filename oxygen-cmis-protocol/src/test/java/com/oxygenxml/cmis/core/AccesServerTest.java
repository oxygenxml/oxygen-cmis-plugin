package com.oxygenxml.cmis.core;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.junit.Assert;
import org.junit.Test;

public class AccesServerTest {

  @Test
  public void testGetRepos() throws MalformedURLException {
    
    List<Repository> repositoryList = CMISAccess.getInstance().connectToServerGetRepositories(new URL("http://localhost:8080/B/atom11"), null);
    
    //to understand what i do
    for(Repository rep : repositoryList) {
      System.out.println(rep.getName() + " ---- " + rep.getId());
    }
    
    Assert.assertNotNull("The repository List should not be null.", repositoryList);
    Assert.assertFalse("The repository list should not be empty", repositoryList.isEmpty());
    Assert.assertEquals("The number of repositories should be 1.", 1, repositoryList.size());
    Assert.assertEquals("Apache Chemistry OpenCMIS InMemory Repository", repositoryList.get(0).getName());
    Assert.assertEquals("A1", repositoryList.get(0).getId());
  }
}


