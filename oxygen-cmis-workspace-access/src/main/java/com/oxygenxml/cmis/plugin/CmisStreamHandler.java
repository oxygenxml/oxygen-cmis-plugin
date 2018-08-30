package com.oxygenxml.cmis.plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.ui.AuthenticatorUtil;
import com.oxygenxml.cmis.ui.UserCanceledException;

public class CmisStreamHandler extends URLStreamHandler {

  @Override
  protected URLConnection openConnection(URL url) throws IOException {
    try {
      System.out.println("URL=" + url);
      URL serverURL = CmisURLConnection.getServerURL(url.toExternalForm(), new HashMap<String, String>());
      UserCredentials uc = AuthenticatorUtil.getUserCredentials(serverURL);

      return new CmisURLConnection(url, CMISAccess.getInstance(), uc);

    } catch (UserCanceledException e) {
      
      e.printStackTrace();
    }
    return null;
  }

}
