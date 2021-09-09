package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.junit.Before;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

public class EditorListenerTest {

  private CMISAccess mockCmisAccess;
  private EditorListener editorListener;
  private Document document;
  
  @Before
  public void setup() throws Exception {
    UserCredentials credentials = new UserCredentials("userLogin", "pass");
    editorListener = new EditorListener();
    mockCmisAccess = mock(CMISAccess.class);
   
    CmisURLConnection cmisConnection  = new CmisURLConnection(new URL("http://server/path"), mockCmisAccess, credentials); 
    editorListener.connection = cmisConnection;
    editorListener.credentials = credentials;
    
    document = mock(Document.class);
  }
  
  
  @Test
  public void testCanEditIfSharepointAndCanSetContentStream() throws Exception {
    when(mockCmisAccess.isSharePoint()).thenReturn(true);
    when(document.hasAllowableAction(Action.CAN_SET_CONTENT_STREAM)).thenReturn(true);
    
    assertTrue(editorListener.canEditDocument(document));
  }
  
  @Test
  public void testCanNOTEditIfSharepointAndNoSetContentStreamAllowableAction() throws Exception {
    when(mockCmisAccess.isSharePoint()).thenReturn(true);
    when(document.hasAllowableAction(Action.CAN_SET_CONTENT_STREAM)).thenReturn(false);
    when(document.getVersionSeriesCheckedOutBy()).thenReturn("other user");
    
    assertFalse(editorListener.canEditDocument(document));
  }
  
  @Test
  public void testCanEditIfNotSharepointAndLoggedUserIsCheckedOutBy() throws Exception {
    when(mockCmisAccess.isSharePoint()).thenReturn(false);
    when(document.hasAllowableAction(Action.CAN_SET_CONTENT_STREAM)).thenReturn(true);
    when(document.getVersionSeriesCheckedOutBy()).thenReturn("userLogin");
    
    assertTrue(editorListener.canEditDocument(document));
  }
  
  @Test
  public void testCanNOTEditIfNotSharepointAndLoggedUserIsNOTCheckedOutBy() throws Exception {
    when(mockCmisAccess.isSharePoint()).thenReturn(false);
    when(document.hasAllowableAction(Action.CAN_SET_CONTENT_STREAM)).thenReturn(true);
    when(document.getVersionSeriesCheckedOutBy()).thenReturn("other user");
    
    assertFalse(editorListener.canEditDocument(document));
  }
}
