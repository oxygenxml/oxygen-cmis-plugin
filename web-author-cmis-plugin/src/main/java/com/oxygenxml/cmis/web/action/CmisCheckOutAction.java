package com.oxygenxml.cmis.web.action;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

public class CmisCheckOutAction {

	private static final Logger logger = Logger.getLogger(CmisCheckOutAction.class.getName());

	public static void checkOutDocument(Document document) throws Exception {

		document = document.getObjectOfLatestVersion(false);

		if (document.isVersionSeriesCheckedOut()) {
			logger.info("Document is checked-out!");

		} else {
			document.checkOut();
			document.refresh();

			logger.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());
		}
	}

	public static void cancelCheckOutDocument(Document document, CmisURLConnection connection) throws Exception {

		if (!document.isVersionSeriesCheckedOut()) {
			logger.info("Document isn't checked-out!");
		} else {
			document = document.getObjectOfLatestVersion(false);

			String pwc = document.getVersionSeriesCheckedOutId();

			if (pwc != null) {
				Document PWC = (Document) connection.getCMISAccess().getSession().getObject(pwc);
				PWC.cancelCheckOut();
			}

			document.refresh();

			logger.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());
		}

	}
}
