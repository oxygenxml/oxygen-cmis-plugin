package com.oxygenxml.cmis.core;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * The CMIS URLs have a repository part which contains the id and the name of the
 * repository in the following format:
 * REPOSITORY_NAME [REPOSITORY_ID]
 *   
 * 
 * @author bogdan_paulon
 *
 */
@Slf4j
public class RepositoryInfo {

  /**
   * The pattern to extract the repository ID from an URL. The repository part of
   * the URL has the format: <repository name> [repository id]
   */
  private static final Pattern EXTRACT_REPO_ID_PATTERN = Pattern.compile("(.*?)\\s\\[(.*?)\\]$");

  private String id;
  private String name;

  public RepositoryInfo(String id, String name) {
    Objects.requireNonNull("the id of the repository cannot be null", id);
    this.id = id;
    this.name = name != null ? name : "";
  }

  @Override
  public String toString() {
    return "RepositoryInfo [id=" + id + ", name=" + name + "]";
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public static RepositoryInfo fromURLPart(String urlPart) {
    Matcher m = EXTRACT_REPO_ID_PATTERN.matcher(urlPart);
    if (m.matches()) {
      String name = m.group(1);
      String id = m.group(2);
      log.debug("Repo name:{} - id: {}", name, id);
      return new RepositoryInfo(id, name);
    }
    return new RepositoryInfo(urlPart, "");
  }

  public String toUrlPart() {
    if (name.isEmpty()) {
      return id;
    } else {
      return name + " [" + id + "]";
    }
  }

}
