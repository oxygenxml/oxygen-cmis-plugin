package com.oxygenxml.cmis.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

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
   * The prefix of a CMIS URL.
   */
  private static final String PREFIX = CMIS_PROTOCOL + "://";
  
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
   * Parses the server URL of the 
   * @param serverRootUrl
   * @return
   * @throws MalformedURLException
   */
  public static URL parseServerUrl(String serverRootUrl) throws MalformedURLException {
    String encodedServerUrl = parseEncodedServerUrl(serverRootUrl);
    return new URL(URLUtil.decodeURIComponent(encodedServerUrl));
  }

  /**
   * Parse the encoded server URL out of a CMIS URL.
   * @param cmisUrl The CMIS URL.
   * @return The server root URL.
   * @throws MalformedURLException
   */
  private static String parseEncodedServerUrl(String cmisUrl) throws MalformedURLException {
    String invalidMsg = "Invalid CMIS URL. ";
    if (!cmisUrl.startsWith(PREFIX)) {
      throw new MalformedURLException(invalidMsg + "Must start with \"" + PREFIX  + "\": " + cmisUrl);
    }
    
    int serverUrlEnd = cmisUrl.indexOf(SLASH_SYMBOL, PREFIX.length() + 1);
    if (serverUrlEnd == -1) {
      throw new MalformedURLException(invalidMsg + "Missing CMIS server URL: " + cmisUrl);
    }
    return cmisUrl.substring(PREFIX.length(), serverUrlEnd);
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
    String invalidMsg = "Invalid CMIS URL. ";
    String encodedServerHttpUrl = parseEncodedServerUrl(cmisUrl);
    String decodedHttpServerUrl = URLUtil.decodeURIComponent(encodedServerHttpUrl);
    URL serverHttpUrl = new URL(decodedHttpServerUrl);
    
    // cmis://[CMIS_SERVER_HOST]/REPOSITORY->/PATH
    int serverUrlEnd = PREFIX.length() + encodedServerHttpUrl.length(); 
    int repositoryEnd = cmisUrl.indexOf(SLASH_SYMBOL, serverUrlEnd + 1);
    if (repositoryEnd == -1) {
      throw new MalformedURLException(invalidMsg + "Missing repository: " + cmisUrl);
    }
    String repository = URLUtil.decodeURIComponent(cmisUrl.substring(
        serverUrlEnd + 1, 
        repositoryEnd));
    
    
    // cmis://[CMIS_SERVER_HOST]/REPOSITORY/[PATH]
    String path = URLUtil.decodeURIComponent(cmisUrl.substring(repositoryEnd));
    
    return new CmisURL(serverHttpUrl, repository, path);
  }

  /**
   * Create the CMIS URL of the given repository.
   * 
   * @param serverHttpUrl The URL of the server.
   * @param repoId The ID of the repository.
   * 
   * @return The CMIS URL.
   */
  public static CmisURL ofRepo(URL serverHttpUrl, String repoId) {
    return new CmisURL(serverHttpUrl, repoId, "");
  }
  
  /**
   * Returns an URL that has the same details as the current instance, but a different path.
   * 
   * @param pathEntries The path (not encoded).
   * 
   * @return The CMIS URL object with the given path.
   */
  public CmisURL setPath(String path) {
    return new CmisURL(serverHttpUrl, repsitory, path);
  }
  
  /**
   * @return The repository ID.
   */
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
  
  /**
   * @return The string representation of a CMIS URL.
   */
  public String toExternalForm() {
    StringBuilder sb = new StringBuilder();
    sb.append(CMIS_PROTOCOL)
      .append("://")
      .append(URLUtil.encodeURIComponent(serverHttpUrl.toExternalForm()))
      .append(SLASH_SYMBOL)
      .append(URLUtil.encodeURIComponent(repsitory))
      .append(SLASH_SYMBOL);

    
    String encodedPath = Arrays.stream(path.split(SLASH_SYMBOL))
      .skip(1) // The first part is an empty string since path starts with '/'
      .map(URLUtil::encodeURIComponent)
      .collect(Collectors.joining(SLASH_SYMBOL));
    
    sb.append(encodedPath);
    return sb.toString();
  }
}
