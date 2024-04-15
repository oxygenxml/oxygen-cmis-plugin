package com.oxygenxml.cmis.core;

public class TokenCredentials implements CmisCredentials {
  private String token;
  private String username;
  public TokenCredentials(String token, String username) {
    this.token = token;
    this.username = username;
  }
  public String getToken() {
    return token;
  }
  @Override
  public boolean isEmpty() {
    return token.isEmpty();
  }
  @Override
  public String getUsername() {
    return username;
  }
}
