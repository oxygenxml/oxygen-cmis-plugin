package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class ResourceControllerTest {

  ResourceController rc;

  @Before
  public void setup() {
    Session session = Mockito.mock(Session.class);
    rc = new ResourceController(session);
  }

  @Test
  public void shouldReturnFalseIfVersionableTypeDefinitionCannotBeFound() {
    Mockito.doThrow(CmisObjectNotFoundException.class)
        .when(rc.getSession())
        .getTypeDefinition(ResourceController.VERSIONABLE_OBJ_TYPE);

    boolean typeExists = rc.isTypeSupported(ResourceController.VERSIONABLE_OBJ_TYPE);

    assertFalse(typeExists);
  }

  /**
   * A call to SharePoint with invalid typeId returns HTTP 400 
   * 
   * e.g.
   * http://n.n.n.n/path/to/repos/_vti_bin/cmis/rest/<repo_id>?getTypeDefinition&typeId=VersionableType
   * 
   * Returns
   * HTTP error code 400 - "One or more of the input parameters to the service method 
   * is missing or invalid." The HTTP error code is translated to CmisInvalidArgumentException
   * 
   * Replacing the type with a correct one (i.e. cmis:document) returns the correct type definition 
   */
  @Test
  public void shouldReturnFalseIfGetVersionableTypeFailsWithInvalidArgument() {
    Session session = Mockito.mock(Session.class);

    ResourceController rc = new ResourceController(session);

    Mockito.doThrow(CmisInvalidArgumentException.class)
        .when(rc.getSession())
        .getTypeDefinition(ResourceController.VERSIONABLE_OBJ_TYPE);

    boolean typeExists = rc.isTypeSupported(ResourceController.VERSIONABLE_OBJ_TYPE);

    assertFalse(typeExists);
  }

  @Test
  public void shouldReturnTrueIfVersionableTypeDefinitionExists() {
    boolean typeExists = rc.isTypeSupported(ResourceController.VERSIONABLE_OBJ_TYPE);

    assertTrue(typeExists);
  }

  @Test
  public void shouldReturnTrueIfCalledWithDefaultObjectType() {
    boolean typeExists = rc.isTypeSupported(ResourceController.DEFAULT_OBJ_TYPE);

    assertTrue(typeExists);
  }
}
