package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

public class CmisCheckInAction {

	private static final Logger logger = Logger.getLogger(CmisCheckInAction.class.getName());

	public static void checkInDocument(Document document, CmisURLConnection connection, String comment)
			throws CmisUnauthorizedException, CmisObjectNotFoundException, MalformedURLException {
		logger.info("tut=>" + comment);
		
		if (!document.isVersionSeriesCheckedOut()) {
			logger.info("Document isn's checked-out!");
		} else {
			document = document.getObjectOfLatestVersion(false);

			String pwc = document.getVersionSeriesCheckedOutId();
			
			if(pwc != null) {
				Document PWC = (Document) connection.getCMISAccess().getSession().getObject(pwc);
				PWC.checkIn(true, null, null, comment);
			}
			
			document.refresh();
			
			logger.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());
		}
	}
}
