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

import java.util.Properties;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Paul Ferraro
 */
public class PropertyReplacementFilterTest
{
	private final Properties properties = new Properties();
	private final XMLStreamReader reader = mock(XMLStreamReader.class);
	private final XMLStreamReader filter = new PropertyReplacementFilter(this.reader, this.properties);
	private final static String PROPERTY_NAME = "ha-jdbc.existing";
	private final static String SYSTEM_PROPERTY = String.format("${%s}", PROPERTY_NAME);
	private final static String PROPERTY_VALUE = "expected";
	
	@BeforeEach
	public void before()
	{
		this.properties.setProperty(PROPERTY_NAME, PROPERTY_VALUE);
	}

	@Test
	public void close() throws XMLStreamException
	{
		this.filter.close();
		
		verify(this.reader).close();
	}

	@Test
	public void getAttributeCount()
	{
		int expected = 10;
		
		when(this.reader.getAttributeCount()).thenReturn(expected);

		int result = this.filter.getAttributeCount();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getAttributeLocalName()
	{
		String expected = "expected";
		int index = 10;
		
		when(this.reader.getAttributeLocalName(index)).thenReturn(expected);

		String result = this.filter.getAttributeLocalName(index);
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getAttributeName()
	{
		QName expected = QName.valueOf("expected");
		int index = 10;
		
		when(this.reader.getAttributeName(index)).thenReturn(expected);

		QName result = this.filter.getAttributeName(index);
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getAttributeNamespace()
	{
		String expected = "expected";
		int index = 10;
		
		when(this.reader.getAttributeNamespace(index)).thenReturn(expected);

		String result = this.filter.getAttributeNamespace(index);
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getAttributePrefix()
	{
		String expected = "expected";
		int index = 10;
		
		when(this.reader.getAttributePrefix(index)).thenReturn(expected);

		String result = this.filter.getAttributePrefix(index);
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getAttributeType()
	{
		String expected = "expected";
		int index = 10;
		
		when(this.reader.getAttributeType(index)).thenReturn(expected);

		String result = this.filter.getAttributeType(index);
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getAttributeValue()
	{
		String expected = "expected";
		int index = 10;
		
		when(this.reader.getAttributeValue(index)).thenReturn(expected);

		String result = this.filter.getAttributeValue(index);
		
		assertThat(result).isEqualTo(expected);
		
		when(this.reader.getAttributeValue(index)).thenReturn(SYSTEM_PROPERTY);
		
		result = this.filter.getAttributeValue(index);
		
		assertThat(result).isEqualTo(PROPERTY_VALUE);
	}

	@Test
	public void getAttributeValueByName()
	{
		String namespaceURI = "url";
		String localName = "name";
		String expected = "expected";
		
		when(this.reader.getAttributeValue(namespaceURI, localName)).thenReturn(expected);

		String result = this.filter.getAttributeValue(namespaceURI, localName);
		
		assertThat(result).isEqualTo(expected);
		
		when(this.reader.getAttributeValue(namespaceURI, localName)).thenReturn(SYSTEM_PROPERTY);
		
		result = this.filter.getAttributeValue(namespaceURI, localName);
		
		assertThat(result).isEqualTo(PROPERTY_VALUE);
	}

	@Test
	public void getCharacterEncodingScheme()
	{
		String expected = "expected";

		when(this.reader.getCharacterEncodingScheme()).thenReturn(expected);
		
		String result = this.filter.getCharacterEncodingScheme();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getElementText() throws XMLStreamException
	{
		String expected = "expected";
		
		when(this.reader.getElementText()).thenReturn(expected);

		String result = this.filter.getElementText();
		
		assertThat(result).isEqualTo(expected);
		
		when(this.reader.getElementText()).thenReturn(String.format("Expect the un%s!", SYSTEM_PROPERTY));
		
		result = this.filter.getElementText();
		
		assertThat(result).isEqualTo("Expect the unexpected!");
	}

	@Test
	public void getEncoding()
	{
		String expected = "expected";

		when(this.reader.getEncoding()).thenReturn(expected);
		
		String result = this.filter.getEncoding();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getEventType()
	{
		int expected = 10;

		when(this.reader.getEventType()).thenReturn(expected);
		
		int result = this.filter.getEventType();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getLocalName()
	{
		String expected = "expected";

		when(this.reader.getLocalName()).thenReturn(expected);
		
		String result = this.filter.getLocalName();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getLocation()
	{
		Location expected = mock(Location.class);

		when(this.reader.getLocation()).thenReturn(expected);
		
		Location result = this.filter.getLocation();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getName()
	{
		QName expected = QName.valueOf("expected");

		when(this.reader.getName()).thenReturn(expected);
		
		QName result = this.filter.getName();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getNamespaceContext()
	{
		NamespaceContext expected = mock(NamespaceContext.class);

		when(this.reader.getNamespaceContext()).thenReturn(expected);
		
		NamespaceContext result = this.filter.getNamespaceContext();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getNamespaceCount()
	{
		int expected = 10;

		when(this.reader.getNamespaceCount()).thenReturn(expected);
		
		int result = this.filter.getNamespaceCount();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getNamespacePrefix()
	{
		String expected = "expected";
		int index = 10;

		when(this.reader.getNamespacePrefix(index)).thenReturn(expected);
		
		String result = this.filter.getNamespacePrefix(index);
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getNamespaceURI()
	{
		String expected = "expected";

		when(this.reader.getNamespaceURI()).thenReturn(expected);
		
		String result = this.filter.getNamespaceURI();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getNamespaceURIByPrefix()
	{
		String expected = "expected";
		String prefix = "prefix";

		when(this.reader.getNamespaceURI(prefix)).thenReturn(expected);
		
		String result = this.filter.getNamespaceURI(prefix);
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getNamespaceURIByIndex()
	{
		String expected = "expected";
		int index = 10;

		when(this.reader.getNamespaceURI(index)).thenReturn(expected);
		
		String result = this.filter.getNamespaceURI(index);
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getPIData()
	{
		String expected = "expected";
		
		when(this.reader.getPIData()).thenReturn(expected);

		String result = this.filter.getPIData();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getPITarget()
	{
		String expected = "expected";
		
		when(this.reader.getPITarget()).thenReturn(expected);

		String result = this.filter.getPITarget();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getPrefix()
	{
		String expected = "expected";
		
		when(this.reader.getPrefix()).thenReturn(expected);

		String result = this.filter.getPrefix();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getProperty()
	{
		String property = "property";
		Object expected = new Object();
		
		when(this.reader.getProperty(property)).thenReturn(expected);
		
		Object result = this.filter.getProperty(property);
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getText()
	{
		String expected = "expected";
		
		when(this.reader.getText()).thenReturn(expected);

		String result = this.filter.getText();
		
		assertThat(result).isEqualTo(expected);
		
		when(this.reader.getText()).thenReturn(String.format("Expect the un%s!", SYSTEM_PROPERTY));
		
		result = this.filter.getText();
		
		assertThat(result).isEqualTo("Expect the unexpected!");
	}

	@Test
	public void getTextCharacters()
	{
		char[] expected = "expected".toCharArray();
		
		when(this.reader.getTextCharacters()).thenReturn(expected);

		char[] result = this.filter.getTextCharacters();
		
		assertThat(result).isEqualTo(expected);
		
		when(this.reader.getTextCharacters()).thenReturn(String.format("Expect the un%s!", SYSTEM_PROPERTY).toCharArray());
		
		result = this.filter.getTextCharacters();
		
		assertThat(result).isEqualTo("Expect the unexpected!".toCharArray());
	}

	@Test
	public void getTextLength()
	{
		int expected = 10;

		when(this.reader.getTextLength()).thenReturn(expected);
		
		int result = this.filter.getTextLength();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getTextStart()
	{
		int expected = 10;

		when(this.reader.getTextStart()).thenReturn(expected);
		
		int result = this.filter.getTextStart();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getVersion()
	{
		String expected = "expected";
		
		when(this.reader.getVersion()).thenReturn(expected);

		String result = this.filter.getVersion();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void hasName()
	{
		when(this.reader.hasName()).thenReturn(true);

		boolean result = this.filter.hasName();
		
		assertThat(result).isTrue();
	}

	@Test
	public void hasNext() throws XMLStreamException
	{
		when(this.reader.hasNext()).thenReturn(true);

		boolean result = this.filter.hasNext();
		
		assertThat(result).isTrue();
	}

	@Test
	public void hasText()
	{
		when(this.reader.hasText()).thenReturn(true);

		boolean result = this.filter.hasText();
		
		assertThat(result).isTrue();
	}

	@Test
	public void isAttributeSpecified()
	{
		int index = 10;
		
		when(this.reader.isAttributeSpecified(index)).thenReturn(true);

		boolean result = this.filter.isAttributeSpecified(index);
		
		assertThat(result).isTrue();
	}

	@Test
	public void isCharacters()
	{
		when(this.reader.isCharacters()).thenReturn(true);

		boolean result = this.filter.isCharacters();
		
		assertThat(result).isTrue();
	}

	@Test
	public void isEndElement()
	{
		when(this.reader.isEndElement()).thenReturn(true);

		boolean result = this.filter.isEndElement();
		
		assertThat(result).isTrue();
	}

	@Test
	public void isStandalone()
	{
		when(this.reader.isStandalone()).thenReturn(true);

		boolean result = this.filter.isStandalone();
		
		assertThat(result).isTrue();
	}

	@Test
	public void isStartElement()
	{
		when(this.reader.isStartElement()).thenReturn(true);

		boolean result = this.filter.isStartElement();
		
		assertThat(result).isTrue();
	}

	@Test
	public void isWhiteSpace()
	{
		when(this.reader.isWhiteSpace()).thenReturn(true);

		boolean result = this.filter.isWhiteSpace();
		
		assertThat(result).isTrue();
	}

	@Test
	public void next() throws XMLStreamException
	{
		int expected = 10;
		
		when(this.reader.next()).thenReturn(expected);

		int result = this.filter.next();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void nextTag() throws XMLStreamException
	{
		int expected = 10;
		
		when(this.reader.nextTag()).thenReturn(expected);

		int result = this.filter.nextTag();
		
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void require() throws XMLStreamException
	{
		int type = 10;
		String uri = "uri";
		String name = "name";

		this.filter.require(type, uri, name);
		
		verify(this.reader).require(type, uri, name);
	}

	@Test
	public void standaloneSet()
	{
		when(this.reader.standaloneSet()).thenReturn(true);

		boolean result = this.filter.standaloneSet();
		
		assertThat(result).isTrue();
	}
}