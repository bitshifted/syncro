/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro.launch;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vladimir Djurovic
 */
public class ConfigFileProcessor {

	private final Document xmlDocument;
	private final XPathFactory xpathFactory;
	private final Path workDirectory;

	public ConfigFileProcessor(Path configFile) throws ParserConfigurationException, SAXException, IOException {
		var docBuilderFactory = DocumentBuilderFactory.newInstance();
		var builder = docBuilderFactory.newDocumentBuilder();
		xmlDocument = builder.parse(configFile.toString());
		xpathFactory = XPathFactory.newInstance();
		this.workDirectory = configFile.getParent();
	}

	public String[] getLaunchCommand() throws XPathExpressionException {
		List<String> commands = new ArrayList<>();
		var xpath = xpathFactory.newXPath();
		commands.add(getJavaExe(xpath));
		commands.addAll(getJvmOptions(xpath));
		commands.addAll(getSystemProperties(xpath));
		commands.addAll(getClasspath(xpath));
		commands.addAll(getModulePath(xpath));

		var splashScreen = getSplashscreen(xpath);
		if(splashScreen != null) {
			commands.add(splashScreen);
		}

		var mainClass = nodeValueAsString(xpath, "//main-class");

		commands.addAll(getModule(xpath, mainClass));

		System.out.println("launch command: " + commands);
		return commands.toArray(new String[commands.size()]);
	}

	private String getJavaExe(XPath xpath) throws XPathExpressionException {
		var value = nodeValueAsString(xpath, "//jvm-dir");
		if(value == null) {
			value = "jre"; //default JVM location
		}
		var jvmBasePath = workDirectory.resolve(value);
		var jvmExePath = jvmBasePath.resolve("bin").resolve("java");
		return jvmExePath.toString();
	}

	private List<String> getJvmOptions(XPath xpath) throws XPathExpressionException {
		var value = nodeValueAsString(xpath, "//jvm-options");
		if(value != null) {
			return Arrays.asList(value.split("\\s"));
		}
		return List.of();
	}

	private List<String> getSystemProperties(XPath xpath) throws XPathExpressionException {
		var value = nodeValueAsString(xpath, "//jvm-properties");
		if(value != null) {
			return Arrays.asList(value.split("\\s"));
		}
		return List.of();
	}

	private List<String> getClasspath(XPath xpath) throws XPathExpressionException {
		var list = new ArrayList<String>();
		var classpath = nodeValueAsString(xpath, "//classpath");
		if(classpath != null && !classpath.isEmpty() && !classpath.isBlank()) {
			list.add("--class-path");
			list.add(classpath);
		}
		return list;
	}

	private List<String> getModulePath(XPath xpath) throws XPathExpressionException {
		var list = new ArrayList<String>();
		var modules = nodeValueAsString(xpath, "//module-path");
		if(modules != null && !modules.isEmpty() && !modules.isBlank()) {
			list.add("--module-path");
			list.add(modules);
		}
		return list;
	}

	private String getSplashscreen(XPath xpath) throws XPathExpressionException {
		var splash = nodeValueAsString(xpath, "//splash-screen");
		if(splash != null && !splash.isBlank() && !splash.isEmpty()) {
			return "-splash:" + splash;
		}
		return null;
	}

	private List<String> getModule(XPath xpath, String mainClass) throws XPathExpressionException {
		var list = new ArrayList<String>();
		var module = nodeValueAsString(xpath, "//module");
		if(module != null && !module.isEmpty() && !module.isBlank()) {
			list.add("--module");
			list.add(module + "/" + mainClass);
		}
		return list;
	}

	private String nodeValueAsString(XPath xpath, String expressionString) throws XPathExpressionException {
		var expression = xpath.compile(expressionString);
		var node = (Element)expression.evaluate(xmlDocument, XPathConstants.NODE);
		return node != null ? node.getTextContent().trim() : null;
	}
}
