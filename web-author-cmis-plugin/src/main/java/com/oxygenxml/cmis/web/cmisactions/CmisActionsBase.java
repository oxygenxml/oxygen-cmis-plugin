package com.oxygenxml.cmis.web.cmisactions;

import java.io.IOException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;

@WebappRestSafe
public class CmisActionsBase implements AuthorOperation {
	private static final Logger logger = Logger.getLogger(CmisActionsBase.class.getName());

	public static CmisURLConnection connection;

	private static final String CHECK_OUT = "cmisCheckout";
	private static final String CHECK_IN = "cmisCheckin";

	private static final String ACTION = "action";
	private static final String OBJ_URL = "url";

	@Override
	public String getDescription() {
		return null;
	}

	// TODO CHECKOUT class
	// TODO CHECKIN class
	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args)
			throws IllegalArgumentException, AuthorOperationException {

		authorAccess.getWorkspaceAccess();
		String actualAction = (String) args.getArgumentValue(ACTION);

		if (actualAction.equals(CHECK_OUT)) {
			try {
				CmisCheckOutAction.checkOutDocument((String) args.getArgumentValue(OBJ_URL));
			} catch (IOException e) {
				logger.info("Object not found | Invalid URL " + e.toString());
			}
		} else if (actualAction.equals(CHECK_IN)) {
			try {
				CmisCheckInAction.checkInDocument((String) args.getArgumentValue(OBJ_URL));
			} catch (IOException e) {
				logger.info("Object not found | Invalid URL " + e.toString());
			}
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

}
