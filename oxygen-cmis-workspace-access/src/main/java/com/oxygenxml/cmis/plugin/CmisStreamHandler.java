package com.oxygenxml.cmis.plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.CmisAccessSingleton;
import com.oxygenxml.cmis.core.CmisURL;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.ui.AuthenticatorUtil;
import com.oxygenxml.cmis.ui.UserCanceledException;

/**
 * CMIS protocol handler. 
 */
public class CmisStreamHandler extends URLStreamHandler {
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(CmisStreamHandler.class);

  @Override
  protected URLConnection openConnection(URL url) throws IOException {
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("URL: " + url);
      }
      URL serverURL = CmisURL.parseServerUrl(url.toExternalForm());
      UserCredentials uc = AuthenticatorUtil.getUserCredentials(serverURL);

      return new CmisURLConnection(url, CmisAccessSingleton.getInstance(), uc);

    } catch (UserCanceledException e) {
      logger.debug("Exception", e);
    }
    return null;
  }

}
