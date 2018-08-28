package com.oxygenxml.cmis.web.action;

import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;

import ro.sync.basic.util.URLUtil;

public class ListOldVersionsAction {
	public static String listOldVersions(Document document, String url) {

		document = document.getObjectOfLatestVersion(false);

		List<Document> oldVersionsList = document.getAllVersions();
		oldVersionsList.remove(oldVersionsList.size() - 1);
		
		StringBuilder oldBuilder = new StringBuilder();

		String docName = document.getName();

		oldBuilder.append("{");

		for (Document oldDoc : oldVersionsList) {
			if (!oldDoc.isPrivateWorkingCopy() || !oldDoc.isVersionSeriesPrivateWorkingCopy()) {
					oldBuilder.append("\"").append(docName + " " + oldDoc.getVersionLabel()).append("\"");
					oldBuilder.append(":").append("[");
					oldBuilder.append("\"").append("?url=").append(URLUtil.encodeURIComponent(url));
					oldBuilder.append("?oldversion=").append(oldDoc.getId()).append("\"");
					oldBuilder.append(",").append("\"").append(oldDoc.getCheckinComment());
					oldBuilder.append("\"").append("]");
					oldBuilder.append(",");
			}
		}

		oldBuilder.replace(oldBuilder.lastIndexOf(","), oldBuilder.lastIndexOf(",") + 1, "");
		oldBuilder.append("}");

		return oldBuilder.toString();
	}
}
