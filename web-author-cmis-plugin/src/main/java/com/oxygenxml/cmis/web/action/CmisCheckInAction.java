package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

public class CmisCheckInAction {

	private static final Logger logger = Logger.getLogger(CmisCheckInAction.class.getName());

	public static void checkInDocument(Document document, String comment)
			throws CmisUnauthorizedException, CmisObjectNotFoundException, MalformedURLException {
		logger.info("tut=>" + comment);
		
		if(!document.isLatestMajorVersion()) {
			document = document.getObjectOfLatestVersion(true);
		}
		
		if(!document.isVersionSeriesCheckedOut()) {
			
			logger.info("Document isn't checked-out!");
			
		} else {
			
			document.checkIn(true, null, document.getContentStream(), comment);
			document.refresh();
			logger.info(document.getName() + " is checked-out: " + document.isVersionSeriesCheckedOut());
			
		}
	}
}
