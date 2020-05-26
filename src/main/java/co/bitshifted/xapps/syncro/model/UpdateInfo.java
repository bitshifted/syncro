/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro.model;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vladimir Djurovic
 */
public class UpdateInfo {
	private final UpdateCheckStatus status;
	private final List<UpdateDetail> details = new ArrayList<>();

	public UpdateInfo(UpdateCheckStatus status, String targetData)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, URISyntaxException{
		this.status = status;
		initTargets(targetData);
	}

	public UpdateInfo(UpdateCheckStatus status) {
		this.status = status;
	}

	public UpdateCheckStatus getStatus() {
		return status;
	}

	public List<UpdateDetail> getDetails() {
		return details;
	}

	private void initTargets(String data) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, URISyntaxException {
		var docBuilderFactory = DocumentBuilderFactory.newInstance();
		var builder = docBuilderFactory.newDocumentBuilder();
		var xmlDocument = builder.parse(new InputSource(new StringReader(data)));
		var xpathFactory = XPathFactory.newInstance();
		var xpath = xpathFactory.newXPath();
		var nodes = (NodeList)xpath.compile("//detail").evaluate(xmlDocument, XPathConstants.NODESET);
		for(int i = 0;i < nodes.getLength();i++) {
			var node = nodes.item(i);
			details.add(UpdateDetail.fromXml(node));
		}
	}

}
