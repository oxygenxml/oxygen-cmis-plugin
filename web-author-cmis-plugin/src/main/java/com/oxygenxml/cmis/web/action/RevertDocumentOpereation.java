package com.oxygenxml.cmis.web.action;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorOperationWithResult;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;

/**
 * Reverts the document to the latest version from repository.
 */
@WebappRestSafe
public class RevertDocumentOpereation extends AuthorOperationWithResult {
  
  @Override
  public String doOperation(AuthorDocumentModel model, ArgumentsMap args)
      throws AuthorOperationException {
     try {
      this.doOperationInternal(model);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
     return null;
  }
  
  private void doOperationInternal(AuthorDocumentModel model) throws MalformedURLException {
    AuthorEditorAccess editorAccess = model.getAuthorAccess().getEditorAccess();
    URL editorUrl = editorAccess.getEditorLocation();
    URL urlWithoutContextId = CmisActionsUtills.getUrlWithoutContextIdAndVersion(editorUrl);
    CmisURLConnection cmisConnection = CmisActionsUtills.getCmisURLConnection(editorUrl);
    Document cmisDocument = (Document) cmisConnection.getCMISObject(urlWithoutContextId.toExternalForm());
    revert(editorAccess, cmisDocument);
  }

  private void revert(AuthorEditorAccess editorAccess, Document cmisDocument) {
    Optional<Document> checkedInCmisDocument = findLatestCheckedInVersion(editorAccess, cmisDocument);
    if (checkedInCmisDocument.isPresent()) {
      ContentStream cmisStream = checkedInCmisDocument.get().getContentStream();
      InputStreamReader contentInputStream = new InputStreamReader(cmisStream.getStream());
      editorAccess.reloadContent(contentInputStream, false);
    } else {
      // do nothing.
    }
  }

  private Optional<Document> findLatestCheckedInVersion(AuthorEditorAccess editorAccess, Document cmisDocument) {
    return cmisDocument.getObjectOfLatestVersion(false)
      .getAllVersions()
      .stream()
      .filter(document -> !this.isPwc(document))
      .findFirst();
  }

  private boolean isPwc(Document version) {
    return Boolean.TRUE.equals(version.isPrivateWorkingCopy())
        || Boolean.TRUE.equals(version.isVersionSeriesPrivateWorkingCopy());
  }
}
