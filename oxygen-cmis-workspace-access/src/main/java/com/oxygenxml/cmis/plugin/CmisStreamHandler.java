package com.oxygenxml.cmis.plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

public class CmisStreamHandler extends URLStreamHandler {

	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		return new CmisURLConnection(url, CMISAccess.getInstance(), null);
	}

}
