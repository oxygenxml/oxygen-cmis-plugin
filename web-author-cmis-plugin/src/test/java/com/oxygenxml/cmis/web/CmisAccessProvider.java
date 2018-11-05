package com.oxygenxml.cmis.web;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.log4j.Logger;
import org.junit.rules.ExternalResource;

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
  private static final Logger logger = Logger.getLogger(CmisAccessProvider.class.getName());

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
    serverUrl = new URL("http://localhost:8080/B/atom11");

    cmisAccess = new CMISAccess();
    cmisAccess.connectToRepo(serverUrl, "A1", new UserCredentials("admin", ""));
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
