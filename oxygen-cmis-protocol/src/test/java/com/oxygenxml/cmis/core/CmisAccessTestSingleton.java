package com.oxygenxml.cmis.core;

/**
 * CMIS Acess singleton to be used from tests.
 * @author cristi_talau
 *
 */
public class CmisAccessTestSingleton {
  /**
   * The CMIS instance.
   */
  private static CMISAccess instance;

  /**
   * @return the instance.
   */
  public static CMISAccess getInstance() {
    if (instance == null) {
      instance = new CMISAccess();
    }
    return instance;
  }
}
