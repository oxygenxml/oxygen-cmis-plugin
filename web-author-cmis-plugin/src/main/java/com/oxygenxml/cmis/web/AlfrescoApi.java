package com.oxygenxml.cmis.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Can connect to Alfresco's specific REST API (not the CMIS API).
 */
public class AlfrescoApi {
  /**
   * The base API URL like "http://alfresco.sync.ro:8080/alfresco/api/-default-/public/alfresco/versions/1/".
   */
  private URL apiBaseUrl;
  
  /**
   * @param cmisatomUrl CMIS Atom URL like "http://alfresco.sync.ro:8080/alfresco/cmisatom".
   */
  public AlfrescoApi(URL cmisatomUrl) {
    try {
      this.apiBaseUrl = new URL(cmisatomUrl, "api/-default-/public/alfresco/versions/1/");
    } catch (MalformedURLException e) {
      throw new UncheckedIOException(e);
    }
  }
  
  /**
   * @param alfrescoTicket The ticket.
   * @return Current user details.
   * @throws IOException If it fails.
   */
  public AlfrescoPeople getMe(String alfrescoTicket)
      throws IOException {
    URL peopleMeApi = new URL(apiBaseUrl, "./people/-me-");
    URLConnection connection = peopleMeApi.openConnection();
    String encodedToken = new String(Base64.getEncoder().encode(alfrescoTicket.getBytes()));
    connection.setRequestProperty("Authorization", "Basic " + encodedToken);
    try (InputStream is = connection.getInputStream()) {
      AlfrescoPeopleEntry peopleEntry = new ObjectMapper().readValue(is, AlfrescoPeopleEntry.class);
      return peopleEntry.getEntry();
    }
  }
}
