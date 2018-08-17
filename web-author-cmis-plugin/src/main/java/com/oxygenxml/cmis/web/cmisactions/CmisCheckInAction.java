package com.oxygenxml.cmis.web.cmisactions;

import java.io.IOException;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.log4j.Logger;

public class CmisCheckInAction extends CmisActionsBase {

	private static final Logger logger = Logger.getLogger(CmisCheckInAction.class.getName());

	public static void checkInDocument(String url) throws IOException {

		Document document = (Document) connection.getCMISObject(url);
		logger.info("!!!!!!!!!!! " + document.getName());
	}
}
