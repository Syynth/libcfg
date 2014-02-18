package com.syynth.libcfg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author syynth
 * @since 2/18/14
 */
public class Config {

	private final Document config;
	private final File file;

	/**
	 * Create a Config document with the file located at <code>name</code>.
	 * @param name
	 */
	public Config(String name) {
		Document cfg;
		file = new File(name);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			cfg = builder.parse(file);
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			cfg = null;
		}
		config = cfg;
	}

	public boolean getBoolean(String name) {
		return Boolean.parseBoolean(getProperty(name));
	}

	public boolean getBoolean(String group, String name) {
		return Boolean.parseBoolean(getProperty(group, name));
	}

	public int getInt(String name) {
		return Integer.parseInt(getProperty(name));
	}

	public int getInt(String group, String name) {
		return Integer.parseInt(getProperty(group, name));
	}

	public float getFloat(String name) {
		return Float.parseFloat(getProperty(name));
	}

	public float getFloat(String group, String name) {
		return Float.parseFloat(getProperty(group, name));
	}

	public double getDouble(String name) {
		return Double.parseDouble(getProperty(name));
	}

	public double getDouble(String group, String name) {
		return Double.parseDouble(getProperty(group, name));
	}

	public String getProperty(String property) {
		return getProperty("global", property);
	}

	public String getProperty(String group, String property) {
		if (config != null) {
			return "";
		}
		XPathFactory factory = XPathFactory.newInstance();
		XPath path = factory.newXPath();
		try {
			return (String) path.evaluate("//propertyGroup[@name='" + group
				+ "']/property[@name='" + property + "']/text()", config, XPathConstants.STRING);
		} catch (XPathExpressionException ex) { return ""; }
	}

	public HashMap<String, String> getPropertyGroup(String groupName) {
		HashMap<String, String> group = new HashMap<>();
		if (config == null) {
			return group;
		}
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			NodeList nl = (NodeList) path.evaluate("//propertyGroup[@name='" + groupName + "']/property", config, XPathConstants.NODESET);
			for (int i = 0; i < nl.getLength(); ++i) {
				Element n = (Element) nl.item(i);
				group.put(n.getAttribute("name"), n.getTextContent());
			}
		} catch (XPathExpressionException ignored) {}
		return group;
	}

	public boolean setProperty(String group, String name, String value) {
		XPathFactory factory = XPathFactory.newInstance();
		XPathExpression xpr;
		XPath xpath = factory.newXPath();
		try {
			xpr = xpath.compile("//propertyGroup[@name='" + group
				+ "']/property[@name='" + name + "']/text()");
			Node n = (Node) xpr.evaluate(config, XPathConstants.NODE);
			n.setNodeValue(value);
			return new XmlDocumentWriter().write(config, file);
		} catch (XPathExpressionException ex) {
			return false;
		}
	}

	private static class XmlDocumentWriter {

		public boolean write(Document doc, File file) {
			TransformerFactory factory = TransformerFactory.newInstance();
			try {
				Transformer transformer = factory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(file);
				transformer.transform(source, result);
			} catch (TransformerException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}

}
