package com.oxygenxml.cmis.core;

import java.net.MalformedURLException;
import java.net.URL;

import ro.sync.basic.util.URLUtil;

/**
 * Class that represents an URL used to identified a CMIS resource. Such an URL has the following form: 
 * 
 * cmis://percent-encoded-serever-http-url/repo/path/to/file.xml
 * 
 * @author ctalau
 *
 */
public class CmisURL {
  /**
   * The scheme used by the CMIS protocol. 
   */
  public static final String CMIS_PROTOCOL = "cmis";

  /**
   * The slash symbol.
   */
  private static final String SLASH_SYMBOL = "/";


  /**
   * The HTTP URL of the CMIS server. 
   */
  private final URL serverHttpUrl;

  /**
   * The name of the repository.
   */
  private final String repsitory;

  /**
   * The path to the file.
   */
  private final String path;
  
  private CmisURL(URL serverHttpUrl, String repsitory, String path) {
    this.serverHttpUrl = serverHttpUrl;
    this.repsitory = repsitory;
    this.path = path;
  }
  
  /**
   * Parses a string representation of a CMIS URL.
   * 
   * @param cmisUrl The string representation of the CMIS URL.
   * @return The CmisURL object.
   * 
   * @throws MalformedURLException if the URL does not have the required format.
   */
  public static CmisURL parse(String cmisUrl) throws MalformedURLException {
    String prefix = CMIS_PROTOCOL + "://";
    if (!cmisUrl.startsWith(prefix)) {
      throw new MalformedURLException(cmisUrl);
    }
    
    int serverUrlEnd = cmisUrl.indexOf(SLASH_SYMBOL, prefix.length() + 1);
    if (serverUrlEnd == -1) {
      throw new MalformedURLException(cmisUrl);
    }
    String encodedServerHttpUrl = cmisUrl.substring(
        prefix.length(),
        serverUrlEnd
    );
    URL serverHttpUrl = new URL(URLUtil.decodeURIComponent(encodedServerHttpUrl));
    
    
    int repositoryEnd = cmisUrl.indexOf(SLASH_SYMBOL, serverUrlEnd + 1);
    if (repositoryEnd == -1) {
      throw new MalformedURLException(cmisUrl);
    }
    String repository = URLUtil.decodeURIComponent(cmisUrl.substring(
        serverUrlEnd + 1, 
        repositoryEnd));
    
    String path = URLUtil.decodeURIComponent(cmisUrl.substring(repositoryEnd));
    
    return new CmisURL(serverHttpUrl, repository, path);
  }

  public String getRepository() {
    return repsitory;
  }

  /**
   * @return the HTTP URL of the CMIS server.
   */
  public URL getServerHttpUrl() {
    return serverHttpUrl;
  }

  /**
   * @return The path of the resource identified by this URL.
   */
  public String getPath() {
    return path;
  }

  /**
   * @return The name of the file represented by this URL.
   */
  public String getFileName() {
    String path = this.getPath();
    return path.substring(path.lastIndexOf(SLASH_SYMBOL) + 1, path.length());
  }
  
  /**
   * @return The name of the folder that contains the file represented by this URL.
   */
  public String getFolderPath() {
    String path = this.getPath();
    return path.substring(0, path.lastIndexOf(SLASH_SYMBOL) + 1);
  }

  /**
   * @return The extension of the file represented by this URL.
   */
  public String getExtension() {
    String fileName = this.getFileName();
    int lastDot = fileName.lastIndexOf('.');
    String extension = "";
    if (lastDot != -1) {
      extension = fileName.substring(lastDot + 1, fileName.length()); 
    }
    return extension;
  }
}
