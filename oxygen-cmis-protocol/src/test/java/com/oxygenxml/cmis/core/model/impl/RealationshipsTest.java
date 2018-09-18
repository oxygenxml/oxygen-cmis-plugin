package com.oxygenxml.cmis.core.model.impl;

import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ConnectionTestBase;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;

public class RealationshipsTest extends ConnectionTestBase{
  private Folder root;
  private ResourceController ctrl;

  /**
   * CONECTION TO SERVER REPOSITORY, ACCES ROOT FOLDER
   * 
   * @throws MalformedURLException
   */
  @Before
  public void setUp() throws MalformedURLException {
    CMISAccess.getInstance().connectToRepo(new URL("http://localhost:8990/alfresco/api/-default-/cmis/versions/1.1/atom"), "-default-",
        new UserCredentials("admin", "1234"));
    ctrl = CMISAccess.getInstance().createResourceController();
    root = ctrl.getRootFolder();
  }
  
  @Test
  public void testRelationshipsDocument() throws CmisConstraintException, UnsupportedEncodingException {
    Document latest = null;
    Document doc = ctrl.createVersionedDocument(root, "queryTestFile3.txt", "some text",
        MimeTypes.getMIMEType("queryTestFile3.txt"), "VersionableType", VersioningState.MINOR);
    Document pwc = (Document) CMISAccess.getInstance().createResourceController().getCmisObj(doc.checkOut().getId());

    List<Relationship> relationships = pwc.getRelationships();
    Iterator<Relationship> relastionIterator = relationships.iterator();
    while (relastionIterator.hasNext()) {
      System.out.println(CMISAccess.getInstance().createResourceController()
          .getCmisObj(relastionIterator.next().getTarget().toString()));
    }
    assertNotNull(relastionIterator);
    
    ctrl.deleteAllVersionsDocument(doc);
  }
  @After
  public void afterMethod() {
  cleanUpDocuments();
    ctrl.getSession().clear();
  }
}
