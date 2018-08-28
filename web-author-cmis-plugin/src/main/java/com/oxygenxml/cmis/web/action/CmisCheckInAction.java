package com.oxygenxml.cmis.web.action;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

public class CmisCheckInAction {

	private static final Logger logger = Logger.getLogger(CmisCheckInAction.class.getName());

	private static final String MAJOR_STATE = "major";

	public static void checkInDocument(Document document, CmisURLConnection connection, String actualState,
			String commitMessage) throws Exception {

		logger.info("Check-In commit: " + commitMessage);

		if (!document.isVersionSeriesCheckedOut()) {
			logger.info("Document isn't checked-out!");

		} else {
			document = document.getObjectOfLatestVersion(false);
			String pwc = document.getVersionSeriesCheckedOutId();

			if (pwc != null) {

				Document PWC = (Document) connection.getCMISAccess().getSession().getObject(pwc);

				logger.info("Check-In actualState: " + actualState);

				if(commitMessage == null || commitMessage == "null") {
					commitMessage = "";
				}
				
				if (actualState.equals(MAJOR_STATE)) {
					PWC.checkIn(true, null, null, commitMessage);
				} else {
					PWC.checkIn(false, null, null, commitMessage);
				}
			}

			document.refresh();

			logger.info(document.getName() + " checked-out: " + document.isVersionSeriesCheckedOut());
		}
	}
}
