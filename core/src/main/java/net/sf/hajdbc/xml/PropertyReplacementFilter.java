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

import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.util.Strings;
import net.sf.hajdbc.util.SystemProperties;

/**
 * Utility class for replacing ${...} placeholders in XML content with system properties.
 * Uses a whitelist to prevent reading sensitive system properties.
 * @author Paul Ferraro
 */
public class PropertyReplacementFilter
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

	private static final Properties SYSTEM_PROPERTIES = SystemProperties.getSystemProperties();

	/**
	 * Replaces ${...} placeholders in the given XML content with system properties.
	 * @param xmlContent the XML content to process
	 * @return the XML content with placeholders replaced
	 */
	public static String replace(String xmlContent)
	{
		return replace(xmlContent, SYSTEM_PROPERTIES);
	}

	/**
	 * Replaces ${...} placeholders in the given content with the specified properties.
	 * @param content the content to process
	 * @param properties the properties to use for replacement
	 * @return the content with placeholders replaced
	 */
	public static String replace(String content, Properties properties)
	{
		if (content == null) return null;

		Matcher matcher = PATTERN.matcher(content);

		if (!matcher.find()) return content;

		StringBuilder builder = new StringBuilder();
		int tail = 0;
		
		do
		{
			builder.append(content, tail, matcher.start());
			
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
				
				String value = getProperty(key.split(","), defaultValue, properties);
				
	  			builder.append((value != null) ? value : matcher.group());
			}
			
			tail = matcher.end();
		}
		while (matcher.find());
		
		builder.append(content, tail, content.length());
		
		return builder.toString();
	}
	
	/**
	 * Resolves a property value from the given properties map.
	 * Supports fallback keys (comma-separated) and default values.
	 * 
	 * <p><b>Security note:</b> If a property key is blocked by the whitelist check,
	 * this method returns {@code null} immediately, bypassing the default value.
	 * This means {@code ${blocked.property:default}} will remain as the literal string
	 * {@code ${blocked.property:default}} in the output, rather than falling back to
	 * {@code default}. This is intentional: allowing a blocked property to resolve via
	 * its default value could be exploited to bypass the whitelist by placing sensitive
	 * data in default values that reference blocked keys.</p>
	 * 
	 * @param keys comma-separated property keys (first non-null value wins)
	 * @param defaultValue fallback value if all keys are null (not used when a key is blocked)
	 * @param properties the properties map to look up values
	 * @return the resolved value, {@code null} if a key was blocked, or the default value if all keys are null
	 */
	private static String getProperty(String[] keys, String defaultValue, Properties properties)
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
			
			String value = properties.getProperty(trimmedKey);
			
			if (value != null) return value;
		}

		return defaultValue;
	}
	
	/**
	 * Check if a property key is allowed based on the whitelist of allowed prefixes.
	 * This prevents reading sensitive system properties that could expose secrets.
	 * Entries ending with '.' are treated as prefix matches (e.g., "ha-jdbc." matches "ha-jdbc.test").
	 * Entries not ending with '.' are treated as exact matches (e.g., "java.version" only matches "java.version",
	 * not "java.version.bogus").
	 * 
	 * @param key the property key to check
	 * @return true if the property is allowed, false otherwise
	 */
	private static boolean isAllowedProperty(String key)
	{
		for (String allowed : ALLOWED_PREFIXES)
		{
			if (allowed.endsWith("."))
			{
				if (key.startsWith(allowed))
				{
					return true;
				}
			}
			else if (key.equals(allowed))
			{
				return true;
			}
		}
		return false;
	}
}
