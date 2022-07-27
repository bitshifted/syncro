/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro.launch;

import co.bitshifted.xapps.syncro.model.ReleaseEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Djurovic
 */
public class ReleaseProcessor {

	private final Document xmlDocument;
	private final XPathFactory xpathFactory;

	public ReleaseProcessor(String configData) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
		xmlDocument = builder.parse(new InputSource(new StringReader(configData)));
		xpathFactory = XPathFactory.newInstance();
	}


    public List<ReleaseEntry> getEntries(Path baseDir) throws XPathExpressionException{
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        String expression = "//entries/*";
        NodeList nodeList = (NodeList) xpath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
		List<ReleaseEntry> entries = new ArrayList<>();
		for(int i = 0;i < nodeList.getLength();i++) {
			Element element = (Element) nodeList.item(i);
			entries.add(new ReleaseEntry(element.getAttribute("sha256"),
				baseDir.resolve(element.getAttribute("target")), Boolean.parseBoolean(element.getAttribute("executable"))));
		}
		return entries;
    }

}
