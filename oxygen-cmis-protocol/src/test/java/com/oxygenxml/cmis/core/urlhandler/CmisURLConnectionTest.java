package com.oxygenxml.cmis.core.urlhandler;

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

public class CmisURLConnectionTest {

  private CMISAccess mockCmisAccess;
  private CmisURLConnection cmisConnection;
  private Document document;
  
  @Before
  public void setup() throws Exception {
    UserCredentials credentials = new UserCredentials("userLogin", "pass");
    mockCmisAccess = mock(CMISAccess.class);
   
    cmisConnection  = new CmisURLConnection(new URL("http://server/path"), mockCmisAccess, credentials); 
    
    document = mock(Document.class);
  }
  
  
  @Test
  public void testCanCheckoutIfSharepointAndCanSetContentStream() throws Exception {
    when(mockCmisAccess.isSharePoint()).thenReturn(true);
    when(document.hasAllowableAction(Action.CAN_SET_CONTENT_STREAM)).thenReturn(true);
    
    assertTrue(cmisConnection.canCheckoutDocument(document));
  }
  
  @Test
  public void testCanNOTCheckoutIfSharepointAndNoSetContentStreamAllowableAction() throws Exception {
    when(mockCmisAccess.isSharePoint()).thenReturn(true);
    when(document.hasAllowableAction(Action.CAN_SET_CONTENT_STREAM)).thenReturn(false);
    when(document.getVersionSeriesCheckedOutBy()).thenReturn("other user");
    
    assertFalse(cmisConnection.canCheckoutDocument(document));
  }
  
  @Test
  public void testCanCheckoutIfNotSharepointAndLoggedUserIsCheckedOutBy() throws Exception {
    when(mockCmisAccess.isSharePoint()).thenReturn(false);
    when(document.hasAllowableAction(Action.CAN_SET_CONTENT_STREAM)).thenReturn(true);
    when(document.getVersionSeriesCheckedOutBy()).thenReturn("userLogin");
    
    assertTrue(cmisConnection.canCheckoutDocument(document));
  }
  
  @Test
  public void testCanNOTCheckoutIfNotSharepointAndLoggedUserIsNOTCheckedOutBy() throws Exception {
    when(mockCmisAccess.isSharePoint()).thenReturn(false);
    when(document.hasAllowableAction(Action.CAN_SET_CONTENT_STREAM)).thenReturn(true);
    when(document.getVersionSeriesCheckedOutBy()).thenReturn("other user");
    
    assertFalse(cmisConnection.canCheckoutDocument(document));
  }
}
