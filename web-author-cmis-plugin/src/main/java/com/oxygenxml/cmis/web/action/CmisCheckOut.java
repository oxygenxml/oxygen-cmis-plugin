package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.TranslationTags;

import lombok.extern.slf4j.Slf4j;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorOperationWithResult;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.commons.operations.ReloadContentOperation;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.ReadOnlyReason;

@WebappRestSafe
@Slf4j
public class CmisCheckOut extends AuthorOperationWithResult {

  /**
   * Do CMIS Check out operation.
   */
  @Override
  public String doOperation(AuthorDocumentModel model, ArgumentsMap args) throws AuthorOperationException {

    AuthorAccess authorAccess = model.getAuthorAccess();
    authorAccess.getWorkspaceAccess();

    URL url = authorAccess.getEditorAccess()
        .getEditorLocation();

    CmisURLConnection connection = CmisActionsUtills.getCmisURLConnection(url);

    // Get Session Store
    String urlWithoutContextId = CmisActionsUtills.getUrlWithoutContextIdAndVersion(url);
    
    Document document = null;
    try {
      document = (Document) connection.getCMISObject(urlWithoutContextId);
    } catch (CmisUnauthorizedException | CmisObjectNotFoundException | MalformedURLException e) {
      log.error("Error getting CMIS document " + urlWithoutContextId);
      throw(new AuthorOperationException(e.getMessage()));
    }

    String actualAction = (String) args.getArgumentValue(CmisAction.ACTION.getValue());

    if (!actualAction.isEmpty() && actualAction.equals(CmisAction.CHECK_OUT.getValue())) {
      try {
        document = document.getObjectOfLatestVersion(false);
        boolean canCheckoutDocument = connection.canCheckoutDocument(document);        
        if (canCheckoutDocument) {
          checkOutDocument(document);
          authorAccess.getEditorAccess().setEditable(true);

          reloadDocument(authorAccess);
        } else {
          WebappPluginWorkspace pluginWorkspace = (WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace();

          PluginResourceBundle rb = pluginWorkspace.getResourceBundle();
          String checkedOutBy = document.getVersionSeriesCheckedOutBy();
          model.getAuthorAccess()
              .getEditorAccess()
              .setReadOnly(new ReadOnlyReason(
                  MessageFormat.format(rb.getMessage(TranslationTags.CHECKED_OUT_BY), checkedOutBy)));

          reloadDocument(authorAccess);

          return CmisActionsUtills.returnErrorInfoJSON("checked_out_by",  
              MessageFormat.format(rb.getMessage(TranslationTags.CANNOT_CHECK_OUT_CHECKED_OUT_BY), document.getName(), checkedOutBy));
        }

      } catch (Exception e) {
        log.info(connection.getUserCredentials().getUsername() + " CANNOT checkout " + document.getName() + " " + e.getMessage());
        return CmisActionsUtills.returnErrorInfoJSON("denied", e.getMessage());
      }
    }

    return CmisActionsUtills.returnErrorInfoJSON("success", null);
  }

  void reloadDocument(AuthorAccess authorAccess) throws AuthorOperationException {
    new ReloadContentOperation().doOperation(authorAccess, argumentName ->
    "markAsNotModified".equals(argumentName) ? "true" : "");
  }

  /**
   * Check out the obtained CMIS Document.
   * 
   * @param document the document to check out
   */
  public static void checkOutDocument(Document document) {
      document.checkOut();
      document.refresh();
  }

}
