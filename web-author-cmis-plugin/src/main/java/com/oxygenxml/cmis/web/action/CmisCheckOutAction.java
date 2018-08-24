package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

public class CmisCheckOutAction {

	private static final Logger logger = Logger.getLogger(CmisCheckOutAction.class.getName());

	public static void checkOutDocument(Document document, CmisURLConnection connection)
			throws CmisUnauthorizedException, CmisObjectNotFoundException, MalformedURLException {

		document = document.getObjectOfLatestVersion(false);

		if (document.isVersionSeriesCheckedOut()) {
			logger.info("Document is checked-out!");

		} else {
			document.checkOut();
			document.refresh();

			logger.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());
		}
	}

	public static void cancelCheckOutDocument(Document document, CmisURLConnection connection)
			throws CmisUnauthorizedException, CmisObjectNotFoundException, MalformedURLException {

		if (!document.isVersionSeriesCheckedOut()) {
			logger.info("Document isn's checked-out!");
		} else {
			document = document.getObjectOfLatestVersion(false);

			String pwc = document.getVersionSeriesCheckedOutId();
			
			if(pwc != null) {
				Document PWC = (Document) connection.getCMISAccess().getSession().getObject(pwc);
				PWC.cancelCheckOut();
			}
			
			document.refresh();
			
			logger.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());
		}

	}
}
