package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.TranslationTags;

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
public class CmisCheckOut extends AuthorOperationWithResult {

  private static final Logger logger = Logger.getLogger(CmisCheckOut.class.getName());

  private CmisURLConnection connection;
  private Document document;

  /**
   * Do CMIS Check out operation.
   */
  @Override
  public String doOperation(AuthorDocumentModel model, ArgumentsMap args) throws AuthorOperationException {

    AuthorAccess authorAccess = model.getAuthorAccess();
    authorAccess.getWorkspaceAccess();

    URL url = authorAccess.getEditorAccess()
        .getEditorLocation();

    connection = CmisActionsUtills.getCmisURLConnection(url);

    // Get Session Store
    String urlWithoutContextId = CmisActionsUtills.getUrlWithoutContextId(url);

    try {
      document = (Document) connection.getCMISObject(urlWithoutContextId);
    } catch (CmisUnauthorizedException | CmisObjectNotFoundException | MalformedURLException e) {
      logger.debug(e.getStackTrace());
    }

    String actualAction = (String) args.getArgumentValue(CmisAction.ACTION.getValue());

    if (!actualAction.isEmpty() && actualAction.equals(CmisAction.CHECK_OUT.getValue())) {
      try {
        if (canCheckoutDocument()) {
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
        return CmisActionsUtills.returnErrorInfoJSON("denied", e.getMessage());
      }
    }

    return CmisActionsUtills.returnErrorInfoJSON("no_error", null);
  }

  @VisibleForTesting
  boolean canCheckoutDocument() {
    Document doc = document.getObjectOfLatestVersion(false);
    Boolean canSetContentStream = doc.hasAllowableAction(Action.CAN_SET_CONTENT_STREAM);
    boolean isSharePoint = connection.getCMISAccess()
        .isSharePoint();

    String versionSeriesCheckedOutBy = doc.getVersionSeriesCheckedOutBy();

    return (canSetContentStream && isSharePoint) || versionSeriesCheckedOutBy == null 
        || connection.getUserCredentials().getUsername().equals(versionSeriesCheckedOutBy);
  }

  /**
   * Reload the document after cancel check out to discard changes.
   * 
   * @param authorAccess
   * @throws AuthorOperationException
   */
  private void reloadDocument(AuthorAccess authorAccess) throws AuthorOperationException {
    ReloadContentOperation reloadAction = new ReloadContentOperation();
    class ActionIdArgumentsMap implements ArgumentsMap {
      @Override
      public Object getArgumentValue(String argumentName) {
        return argumentName.equals("markAsNotModified") ? true : "";
      }
    }
    ArgumentsMap argMap = new ActionIdArgumentsMap();
    reloadAction.doOperation(authorAccess, argMap);
  }

  /**
   * Check out the obtained CMIS Document.
   * 
   * @param document the document to check out
   */
  public static void checkOutDocument(Document document) {

    document = document.getObjectOfLatestVersion(false);

    if (document.isVersionSeriesCheckedOut()) {
      logger.info("Document was checked-out!");
    } else {
      document.checkOut();
      document.refresh();
      logger.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());
    }
  }

}
