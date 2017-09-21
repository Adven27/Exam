package com.adven.concordion.extensions.exam.files.commands;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static com.google.common.base.Strings.isNullOrEmpty;

class XmlFormatter {

    public String format(String unformattedXml) throws IOException {
        if (isNullOrEmpty(unformattedXml)) {
            return "";
        }
        Document document = parseXmlFile(unformattedXml);

        OutputFormat format = new OutputFormat(document);
        format.setLineWidth(65);
        format.setIndenting(true);
        format.setIndent(2);
        Writer out = new StringWriter();
        new XMLSerializer(out, format).serialize(document);

        return out.toString();
    }

    private Document parseXmlFile(String in) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return db.parse(new InputSource(new StringReader(in)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}