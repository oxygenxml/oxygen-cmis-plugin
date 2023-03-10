package com.oxygenxml.cmis.web.action;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;

import org.apache.chemistry.opencmis.client.api.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.web.CredentialsManager;

import lombok.extern.slf4j.Slf4j;
import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContextUtil;

@Slf4j
public class CmisActionsUtills {

	/**
	 * Not meant to be instantiated.
	 */
	private CmisActionsUtills() {}
	
	/**
	 * JSON Builder for error details.
	 * 
	 * @param errorType
	 * @param errorInfo
	 * @return Error info JSON String.
	 * @throws IOException 
	 */
	private static String getErrorInfoJSON(String errorType, String errorInfo) throws IOException {	
		HashMap<String, String> errorMap = new HashMap<>();
		errorMap.put("error", errorType);
		
		if (errorInfo != null && !errorMap.isEmpty()) {
			errorMap.put("message", errorInfo);
		}
		
		return new ObjectMapper().writeValueAsString(errorMap);
	}

	/**
	 * Get JSON result from errorInfoBuilder,
	 * Handle exceptions if it's thrown.
	 * 
	 * @param errorType
	 * @param errorMessage
	 * @return the JSON string 
	 */
	public static String returnErrorInfoJSON(String errorType, String errorMessage) {
		String errorInfoJSON = null;
		
		try {
			errorInfoJSON = CmisActionsUtills.getErrorInfoJSON(errorType, errorMessage);
		} catch (IOException e) {
			log.debug(e.getMessage());
		}
		
		return errorInfoJSON;
	}
	
	/**
	 * Open connection for CMIS operations.
	 * 
	 * @param url
	 * @return CmisURLConnetion for AuthorOperations.
	 */
	public static CmisURLConnection getCmisURLConnection(URL url)  {
		String contextId = url.getUserInfo();
		UserCredentials credentials = CredentialsManager.INSTANCE.getCredentials(contextId);
		
		log.info("Getting connection!");
		
		return new CmisURLConnection(url, new CMISAccess(), credentials);
	}
	
	 /**
   * Removing Context Id from URL and version.
   * 
   * @param url The OXY URL.
   * @return URL as String without Context Id and without version (oldversion query param).
   */
  public static String getUrlWithoutContextIdAndVersion(URL url) {
    return stripVersion(getUrlWithoutContextId(url));
  }
	
	/**
	 * Removing Context Id from URL.
	 * 
	 * @param url
	 * @return URL as String without Context Id.
	 */
	public static String getUrlWithoutContextId(URL url) {
		String urlWithoutContextId = URLStreamHandlerWithContextUtil.getInstance().toStrippedExternalForm(url);
		return urlWithoutContextId;
  }

  private static String stripVersion(String url) {
    if (url.contains(CmisAction.OLD_VERSION.getValue()) || url.contains("?")) {
      url = url.substring(0, url.indexOf('?'));
    }
    return url;
  }
	
  public static Optional<String> getVersionId(URL url) {
    Optional<String> toReturn = Optional.empty();
    String query = url.getQuery();
    if (query != null) {
      HashMap<String, String> queryPart = new HashMap<>();
      for (String pair : url.getQuery().split("&")) {
        int index = pair.indexOf('=');
        queryPart.put(pair.substring(0, index), pair.substring(index + 1));
      }
      String objectId = queryPart.get(CmisAction.OLD_VERSION.getValue());
      toReturn = Optional.ofNullable(objectId);
    }
    return toReturn;
  }

  public static Document getLatestVersion(Document document) {
    Document latest = document;
    if (!Boolean.TRUE.equals(document.isLatestVersion())) {
      latest = document.getObjectOfLatestVersion(false);
    }
    return latest;
  }
}
