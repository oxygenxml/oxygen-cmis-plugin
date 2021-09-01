package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RepositoryInfoTest {

  @Test
  public void extractsRepositoryInfoFromURLPart() {
    String id = "78ad70d4-396c-4a1e-9438-f38f22510b1d";
    RepositoryInfo repo = RepositoryInfo.fromURLPart("Repository Name ["+ id +"]");
    assertEquals(id, repo.getId());
    assertEquals("Repository Name", repo.getName());
  }
  
  @Test
  public void extractsRepositoryInfoFromURLPartWithIdOnly() {
    String id = "78ad70d4-396c-4a1e-9438-f38f22510b1d";
    RepositoryInfo repo = RepositoryInfo.fromURLPart(id);
    assertEquals(id, repo.getId());
    assertEquals("", repo.getName());
  }
}
