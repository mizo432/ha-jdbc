/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2014  Paul Ferraro
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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.util.Strings;
import net.sf.hajdbc.util.SystemProperties;

public class PropertyReplacementFilter extends StreamReaderDelegate
{
	private static final Pattern PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

	// Whitelist of allowed property prefixes to prevent reading sensitive system properties
	// Only specific safe properties are allowed; broad prefixes like "java." and "user." are avoided
	// to prevent leaking sensitive information (e.g., java.security.auth.login.config, user.home).
	private static final List<String> ALLOWED_PREFIXES = Arrays.asList(
			"ha-jdbc.",           // HA-JDBC specific properties
			"net.sf.hajdbc.",     // HA-JDBC internal properties
			"java.version",       // Java version
			"java.home",          // Java home directory
			"java.io.tmpdir",     // Temporary directory
			"java.class.path",    // Class path
			"java.library.path",  // Library path
			"user.name",          // User name
			"user.language",      // User language
			"user.country",       // User country
			"user.timezone",      // User timezone
			"os.name",            // OS name
			"os.arch",            // OS architecture
			"os.version",         // OS version
			"file.separator",     // File separator
			"file.encoding",      // File encoding
			"path.separator",     // Path separator
			"jgroups.",           // JGroups properties
			"spring.",            // Spring framework properties
			"javax.xml.",         // XML-related properties
			"awt.",               // AWT properties (font settings, etc.)
			"sun.java2d.",        // Java2D properties
			"line.",              // Line separator property
			"networkaddress.",    // Cache control property
			"com.sun.jndi."       // JNDI properties
			);

	private static final Logger logger = LoggerFactory.getLogger(PropertyReplacementFilter.class);

	private final Properties properties;
	
	public PropertyReplacementFilter(XMLStreamReader reader)
	{
		this(reader, SystemProperties.getSystemProperties());
	}

	public PropertyReplacementFilter(XMLStreamReader reader, Properties properties)
	{
		super(reader);
		this.properties = properties;
	}

	@Override
	public String getAttributeValue(int index)
	{
		return this.replace(super.getAttributeValue(index));
	}

	@Override
	public String getAttributeValue(String namespaceUri, String localName)
	{
		return this.replace(super.getAttributeValue(namespaceUri, localName));
	}

	@Override
	public String getElementText() throws XMLStreamException
	{
		return this.replace(super.getElementText());
	}
	
	@Override
	public String getText()
	{
		return this.replace(super.getText());
	}

	@Override
	public char[] getTextCharacters()
	{
		return this.replace(String.valueOf(super.getTextCharacters())).toCharArray();
	}

	@Override
	public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException
	{
		throw new UnsupportedOperationException();
	}

	private String replace(String input)
	{
		if (input == null) return null;

		Matcher matcher = PATTERN.matcher(input);

		if (!matcher.find()) return input;

		StringBuilder builder = new StringBuilder();
		int tail = 0;
		
		do
		{
			builder.append(input, tail, matcher.start());
			
			String group = matcher.group(1);
			
			if (group.equals("/"))
			{
				builder.append(Strings.FILE_SEPARATOR);
			}
			else if (group.equals(":"))
			{
				builder.append(Strings.PATH_SEPARATOR);
			}
			else
			{
				String key = group;
				String defaultValue = null;
				
				int index = group.indexOf(":");
				
				if (index > 0)
				{
					key = group.substring(0, index);
					defaultValue = group.substring(index + 1);
				}
				
				String value = this.getProperty(key.split(","), defaultValue);
				
	  			builder.append((value != null) ? value : matcher.group());
			}
			
			tail = matcher.end();
		}
		while (matcher.find());
		
		builder.append(input, tail, input.length());
		
		return builder.toString();
	}
	
	private String getProperty(String[] keys, String defaultValue)
	{
		for (String key: keys)
		{
			String trimmedKey = key.trim();
			
			// Check if the property key is allowed by the whitelist
			if (!isAllowedProperty(trimmedKey))
			{
				logger.log(Level.WARN, "Property access blocked: ''{0}'' is not in the allowed property prefixes list", trimmedKey);
				return null;
			}
			
			String value = this.properties.getProperty(trimmedKey);
			
			if (value != null) return value;
		}

		return defaultValue;
	}
	
	/**
	 * Check if a property key is allowed based on the whitelist of allowed prefixes.
	 * This prevents reading sensitive system properties that could expose secrets.
	 * 
	 * @param key the property key to check
	 * @return true if the property is allowed, false otherwise
	 */
	private boolean isAllowedProperty(String key)
	{
		for (String prefix : ALLOWED_PREFIXES)
		{
			if (key.startsWith(prefix))
			{
				return true;
			}
		}
		return false;
	}
}
