package com.oxygenxml.cmis;

import com.oxygenxml.cmis.core.CMISAccess;

/**
 * Singleton holder for CMIS Access.
 * 
 * @author cristi_talau
 */
public class CmisAccessSingleton {

  /**
   * Do not instantiate - singleton holder class.
   */
  private CmisAccessSingleton() {
  }
  /**
   * The singleton instance.
   */
  private static final CMISAccess INSTANCE = new CMISAccess();
  
  /**
   * @return the singleton CMIS access.
   */
  public static CMISAccess getInstance() {
    return INSTANCE;
  }
  
}
