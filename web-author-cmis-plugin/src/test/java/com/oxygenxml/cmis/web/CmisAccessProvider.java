package com.oxygenxml.cmis.web;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.basic.util.URLStreamHandlerFactorySetter;
import ro.sync.net.protocol.OxygenURLStreamHandlerFactory;

/**
 * Builds a CMIS access.
 * 
 * @author cristi_talau
 */
public class CmisAccessProvider extends ExternalResource{
  private static final Logger logger = LoggerFactory.getLogger(CmisAccessProvider.class.getName());

  static {
    // Logging environment.
    logger.info("Props: " + System.getProperties());
    logger.info("Env: " + System.getenv());
  }
  /**
   * The current CMIS access.
   */
  private CMISAccess cmisAccess;
  /**
   * The server URL.
   */
  private URL serverUrl;
  /**
   * URL factory setter.
   */
  private URLStreamHandlerFactorySetter setter;

  @Override
  protected void before() throws Throwable {
    cmisAccess = createCmisAccessForUserName("admin");
    setter = new URLStreamHandlerFactorySetter();
    OxygenURLStreamHandlerFactory oxygenFactory = new OxygenURLStreamHandlerFactory();
    setter.setFactory(protocol -> {
       if ("cmis".equals(protocol)) {
         return new URLStreamHandler() {
           @Override
           protected URLConnection openConnection(URL u) throws IOException {
             return createConnection(u);
           }
         };
       } else {
         return oxygenFactory.createURLStreamHandler(protocol); 
       }
     });

  }

  public CMISAccess createCmisAccessForUserName(String userName) throws MalformedURLException {
    serverUrl = new URL("http://localhost:8080/B/atom11");

    CMISAccess cmisAccess = new CMISAccess();
    cmisAccess.connectToRepo(serverUrl, "A1", new UserCredentials(userName, ""));
    return cmisAccess;
  }
  
  @Override
  protected void after() {
    try {
      setter.tearDown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public CMISAccess getCmisAccess() {
    return cmisAccess;
  }
  
  public CmisURLConnection createConnection(URL url) {
    return new CmisURLConnection(url, cmisAccess, new UserCredentials("admin", ""));
  }
}
