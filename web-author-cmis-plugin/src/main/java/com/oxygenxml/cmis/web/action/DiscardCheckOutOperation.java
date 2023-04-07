package com.oxygenxml.cmis.web.action;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorOperationWithResult;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;

/**
 * Discards the Check Out but without reloading the content.
 */
@WebappRestSafe
public class DiscardCheckOutOperation extends AuthorOperationWithResult {

  @Override
  public String doOperation(AuthorDocumentModel model, ArgumentsMap args)
      throws IllegalArgumentException, AuthorOperationException {
    this.doOperationInternal(model);
    return null;
  }
  
  private void doOperationInternal(AuthorDocumentModel model) throws CmisObjectNotFoundException {
    AuthorAccess authorAccess = model.getAuthorAccess();
    URL url = authorAccess.getEditorAccess().getEditorLocation();
    CmisURLConnection connection = CmisActionsUtills.getCmisURLConnection(url);
    String urlWithoutContextId = CmisActionsUtills.getUrlWithoutContextId(url);
    try {
      Document document = (Document) connection.getCMISObject(urlWithoutContextId);
      discardPwc(document);
    } catch (MalformedURLException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void discardPwc(Document document) {
    Document latest = document.getObjectOfLatestVersion(false);
    if (latest.isVersionSeriesCheckedOut()) {
      latest.cancelCheckOut();
    } else {
      throw new IllegalStateException();
    }
  }
}
