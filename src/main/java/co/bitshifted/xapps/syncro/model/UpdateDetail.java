/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro.model;

import org.w3c.dom.Node;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Vladimir Djurovic
 */
public class UpdateDetail {

	private final String fileName;
	private final URI uri;
	private final long size;

	public UpdateDetail(String fileName, URI url, long size) {
		this.fileName = fileName;
		this.uri = url;
		this.size = size;
	}

	public String getFileName() {
		return fileName;
	}

	public URI getUri() {
		return uri;
	}

	public long getSize() {
		return size;
	}

	public static UpdateDetail fromXml(Node node) throws  URISyntaxException {
		return null;
//		var list = node.getChildNodes();
//		String name = null;
//		URI url = null;
//		long size = 0;
//		for(int i = 0;i < list.getLength();i++) {
//			var current = list.item(i);
//			switch (current.getNodeName()) {
//				case "fileName":
//					name = current.getTextContent().trim();
//					break;
//				case "url":
//					url = new URI(current.getTextContent().trim().replaceAll("&amp;", "&"));
//					break;
//				case "size":
//					size = Long.parseLong(current.getTextContent().trim());
//					break;
//			}
//		}
//		return new UpdateDetail(name, url, size);
	}
}
