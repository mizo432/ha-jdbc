/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2012  Paul Ferraro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.hajdbc.xml;

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FormattedXMLStreamWriterTest
{
	@Test
	public void writeSimpleElement() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter delegate = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.writeStartElement("root");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("<root>"));
		Assertions.assertTrue(result.contains("</root>"));
	}
	
	@Test
	public void writeNestedElements() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter delegate = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.writeStartElement("root");
		writer.writeStartElement("child");
		writer.writeCharacters("text");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("<root>"));
		Assertions.assertTrue(result.contains("<child>"));
		Assertions.assertTrue(result.contains("text"));
		Assertions.assertTrue(result.contains("</child>"));
		Assertions.assertTrue(result.contains("</root>"));
	}
	
	@Test
	public void writeElementWithAttribute() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter delegate = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.writeStartElement("root");
		writer.writeAttribute("id", "1");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("id=\"1\""));
	}
	
	@Test
	public void writeEmptyElement() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter delegate = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.writeStartElement("root");
		writer.writeEmptyElement("empty");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("<empty"));
	}
	
	@Test
	public void writeComment() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter delegate = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.writeComment("test comment");
		writer.writeStartElement("root");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("test comment"));
	}
	
	@Test
	public void writeCData() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter delegate = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.writeStartElement("root");
		writer.writeCData("CDATA content");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("CDATA content"));
	}
	
	@Test
	public void writeStartElementWithNamespace() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		XMLStreamWriter delegate = factory.createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.writeStartElement("root");
		writer.writeDefaultNamespace("http://example.com");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("root"));
	}
	
	@Test
	public void writeMultipleNestedLevels() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter delegate = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.writeStartElement("level1");
		writer.writeStartElement("level2");
		writer.writeStartElement("level3");
		writer.writeCharacters("deep");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("level1"));
		Assertions.assertTrue(result.contains("level2"));
		Assertions.assertTrue(result.contains("level3"));
		Assertions.assertTrue(result.contains("deep"));
	}
	
	@Test
	public void writeStartElementWithNamespaceURI() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		XMLStreamWriter delegate = factory.createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.writeStartElement("root");
		writer.writeDefaultNamespace("http://example.com");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("root"));
		Assertions.assertTrue(result.contains("http://example.com"));
	}
	
	@Test
	public void writeStartDocumentWithVersion() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter delegate = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument("1.0");
		writer.writeStartElement("root");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("version"));
	}
	
	@Test
	public void writeStartDocumentWithEncodingAndVersion() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter delegate = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument("UTF-8", "1.0");
		writer.writeStartElement("root");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("UTF-8"));
	}
	
	@Test
	public void writeCharactersCharArray() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter delegate = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.writeStartElement("root");
		char[] chars = {'h', 'e', 'l', 'l', 'o'};
		writer.writeCharacters(chars, 0, chars.length);
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("hello"));
	}
	
	@Test
	public void writeProcessingInstruction() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter delegate = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.writeProcessingInstruction("xml-stylesheet", "href=\"style.css\"");
		writer.writeStartElement("root");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("xml-stylesheet"));
	}
	
	@Test
	public void writeDefaultNamespace() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		XMLStreamWriter delegate = factory.createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.writeStartElement("root");
		writer.writeDefaultNamespace("http://example.com");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("root"));
	}
	
	@Test
	public void flush() throws XMLStreamException
	{
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter delegate = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
		FormattedXMLStreamWriter writer = new FormattedXMLStreamWriter(delegate);
		
		writer.writeStartDocument();
		writer.flush();
		writer.writeStartElement("root");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		String result = stringWriter.toString();
		Assertions.assertTrue(result.contains("root"));
	}
}
