package com.oxygenxml.cmis.web.action;

import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;

import ro.sync.basic.util.URLUtil;

public class ListOldVersionsAction {

	public static String listOldVersions(Document document, String url) {

		if (url.contains(CmisActions.OLD_VERSION)) {
			url = url.substring(0, url.indexOf("?"));
		}

		document = document.getObjectOfLatestVersion(false);

		List<Document> oldVersionsList = document.getAllVersions();
		oldVersionsList.remove(oldVersionsList.size() - 1);

		StringBuilder oldBuilder = new StringBuilder();

		oldBuilder.append("{");
		
		for (Document oldDoc : oldVersionsList) {
			if(oldDoc.getVersionLabel().equals("pwc")) {
				continue;
			}
			
			oldBuilder.append("\"").append("v" + oldDoc.getVersionLabel()).append("\"");
			oldBuilder.append(":").append("[");
			oldBuilder.append("\"").append("?url=").append(URLUtil.encodeURIComponent(url));
			oldBuilder.append("?oldversion=").append(oldDoc.getId()).append("\"");
			
			String checkInComment = null;
			
			if(oldDoc.getCheckinComment() == null) {
				checkInComment = "";
			} else {
				checkInComment = oldDoc.getCheckinComment();
				checkInComment = checkInComment.replaceAll("\\n", "<br/>");
			}
			
			oldBuilder.append(",").append("\"").append(checkInComment).append("\"");
			oldBuilder.append(",").append("\"").append(oldDoc.getLastModifiedBy());
			oldBuilder.append("\"").append("]");
			oldBuilder.append(",");
		}

		oldBuilder.replace(oldBuilder.lastIndexOf(","), oldBuilder.lastIndexOf(",") + 1, "");
		oldBuilder.append("}");

		return oldBuilder.toString();
	}
}
