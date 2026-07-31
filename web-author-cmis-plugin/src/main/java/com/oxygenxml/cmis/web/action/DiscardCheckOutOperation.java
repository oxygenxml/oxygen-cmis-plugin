package com.oxygenxml.cmis.web.action;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.EditorListener;
import com.oxygenxml.cmis.web.TranslationTags;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorOperationWithResult;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.ReadOnlyReason;

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
    URL urlWithoutContextId = CmisActionsUtills.getUrlWithoutContextIdAndVersion(url);
    try {
      String urlString = urlWithoutContextId.toExternalForm();
      Document document = (Document) connection.getCMISObject(urlString);
      discardPwc(document);

      if (EditorListener.isCheckOutRequired()) {
        PluginResourceBundle rb = ((WebappPluginWorkspace) PluginWorkspaceProvider
            .getPluginWorkspace()).getResourceBundle();
        authorAccess.getEditorAccess()
            .setReadOnly(new ReadOnlyReason(rb.getMessage(TranslationTags.CHECK_OUT_REQUIRED)));
      }
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
