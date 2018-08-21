package com.oxygenxml.cmis.web.cmisactions;

import java.net.MalformedURLException;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

public class CmisCheckOutAction {

	private static final Logger logger = Logger.getLogger(CmisCheckOutAction.class.getName());

	public static Boolean checkOutDocument(Document document, CmisURLConnection connection)
			throws CmisUnauthorizedException, CmisObjectNotFoundException, MalformedURLException {


		if (!document.isLatestMajorVersion()) {
			document = document.getObjectOfLatestVersion(true);
		}

		Boolean isCheckedOut = document.isVersionSeriesCheckedOut();

		if (isCheckedOut) {
			// TODO: check without if | try to edit working copy
			logger.info("Document is checked-out!");

		} else {
			
			document.checkOut();
			document.refresh();

			logger.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());

		}

		return isCheckedOut;
	}

	public static void cancelCheckOutDocument(Document document, CmisURLConnection connection)
			throws CmisUnauthorizedException, CmisObjectNotFoundException, MalformedURLException {
		

		if (!document.isLatestMajorVersion()) {
			document = document.getObjectOfLatestVersion(true);
		}

		if (!document.isVersionSeriesCheckedOut()) {

			logger.info("Document isn's checked-out!");

		} else {

			document.cancelCheckOut();
			document.refresh();

			logger.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());
		}

	}
}
