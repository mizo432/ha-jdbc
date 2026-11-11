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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for PropertyReplacementFilter static replace method.
 * @author Paul Ferraro
 */
public class PropertyReplacementFilterTest
{
	private final Properties properties = new Properties();

	@Test
	public void replaceSimpleProperty()
	{
		this.properties.setProperty("ha-jdbc.test", "value");
		assertThat(PropertyReplacementFilter.replace("${ha-jdbc.test}", this.properties)).isEqualTo("value");
	}

	@Test
	public void replaceMultipleProperties()
	{
		this.properties.setProperty("ha-jdbc.a", "first");
		this.properties.setProperty("ha-jdbc.b", "second");
		assertThat(PropertyReplacementFilter.replace("${ha-jdbc.a} and ${ha-jdbc.b}", this.properties))
			.isEqualTo("first and second");
	}

	@Test
	public void replaceWithDefaultValue()
	{
		// When property is missing and a default is provided, the default is used
		assertThat(PropertyReplacementFilter.replace("${ha-jdbc.missing:default}", this.properties))
			.isEqualTo("default");
	}

	@Test
	public void replaceSlashWithFileSeparator()
	{
		assertThat(PropertyReplacementFilter.replace("${/}", this.properties))
			.isEqualTo(System.getProperty("file.separator"));
	}

	@Test
	public void replaceColonWithPathSeparator()
	{
		assertThat(PropertyReplacementFilter.replace("${:}", this.properties))
			.isEqualTo(System.getProperty("path.separator"));
	}

	@Test
	public void noReplacementWhenNoPlaceholder()
	{
		assertThat(PropertyReplacementFilter.replace("plain text", this.properties)).isEqualTo("plain text");
	}

	@Test
	public void nullInputReturnsNull()
	{
		assertThat(PropertyReplacementFilter.replace(null, this.properties)).isNull();
	}

	@Test
	public void allowedJavaProperties()
	{
		this.properties.setProperty("java.version", "25.0.1");
		this.properties.setProperty("java.home", "/usr/lib/jvm");
		this.properties.setProperty("java.io.tmpdir", "/tmp");
		assertThat(PropertyReplacementFilter.replace("${java.version}", this.properties)).isEqualTo("25.0.1");
		assertThat(PropertyReplacementFilter.replace("${java.home}", this.properties)).isEqualTo("/usr/lib/jvm");
		assertThat(PropertyReplacementFilter.replace("${java.io.tmpdir}", this.properties)).isEqualTo("/tmp");
	}

	@Test
	public void blockedSensitiveJavaProperties()
	{
		this.properties.setProperty("java.security.auth.login.config", "/etc/jaas.conf");
		assertThat(PropertyReplacementFilter.replace("${java.security.auth.login.config}", this.properties))
			.isEqualTo("${java.security.auth.login.config}");
	}

	@Test
	public void allowedUserProperties()
	{
		this.properties.setProperty("user.name", "testuser");
		this.properties.setProperty("user.language", "en");
		assertThat(PropertyReplacementFilter.replace("${user.name}", this.properties)).isEqualTo("testuser");
		assertThat(PropertyReplacementFilter.replace("${user.language}", this.properties)).isEqualTo("en");
	}

	@Test
	public void blockedSensitiveUserProperties()
	{
		this.properties.setProperty("user.home", "/home/testuser");
		assertThat(PropertyReplacementFilter.replace("${user.home}", this.properties))
			.isEqualTo("${user.home}");
	}

	@Test
	public void allowedOsProperties()
	{
		this.properties.setProperty("os.name", "Linux");
		this.properties.setProperty("os.arch", "amd64");
		assertThat(PropertyReplacementFilter.replace("${os.name}", this.properties)).isEqualTo("Linux");
		assertThat(PropertyReplacementFilter.replace("${os.arch}", this.properties)).isEqualTo("amd64");
	}

	@Test
	public void blockedSensitiveOsProperties()
	{
		this.properties.setProperty("os.environment", "production");
		assertThat(PropertyReplacementFilter.replace("${os.environment}", this.properties))
			.isEqualTo("${os.environment}");
	}

	@Test
	public void allowedFileProperties()
	{
		this.properties.setProperty("file.separator", "/");
		this.properties.setProperty("file.encoding", "UTF-8");
		assertThat(PropertyReplacementFilter.replace("${file.separator}", this.properties)).isEqualTo("/");
		assertThat(PropertyReplacementFilter.replace("${file.encoding}", this.properties)).isEqualTo("UTF-8");
	}

	@Test
	public void blockedSensitiveFileProperties()
	{
		this.properties.setProperty("file.create.onExit", "true");
		assertThat(PropertyReplacementFilter.replace("${file.create.onExit}", this.properties))
			.isEqualTo("${file.create.onExit}");
	}

	@Test
	public void exactMatchPreventsPrefixLeak()
	{
		// "java.version" is an exact match in the whitelist, so "java.version.bogus" should be blocked
		this.properties.setProperty("java.version", "25.0.1");
		this.properties.setProperty("java.version.bogus", "leaked");
		assertThat(PropertyReplacementFilter.replace("${java.version}", this.properties)).isEqualTo("25.0.1");
		assertThat(PropertyReplacementFilter.replace("${java.version.bogus}", this.properties))
			.isEqualTo("${java.version.bogus}");

		// Same for "user.name" — "user.name.secret" should be blocked
		this.properties.setProperty("user.name", "testuser");
		this.properties.setProperty("user.name.secret", "secret-value");
		assertThat(PropertyReplacementFilter.replace("${user.name}", this.properties)).isEqualTo("testuser");
		assertThat(PropertyReplacementFilter.replace("${user.name.secret}", this.properties))
			.isEqualTo("${user.name.secret}");

		// Prefix entries with trailing dot should still allow sub-properties
		this.properties.setProperty("ha-jdbc.test", "value");
		this.properties.setProperty("ha-jdbc.test.nested", "nested-value");
		assertThat(PropertyReplacementFilter.replace("${ha-jdbc.test}", this.properties)).isEqualTo("value");
		assertThat(PropertyReplacementFilter.replace("${ha-jdbc.test.nested}", this.properties)).isEqualTo("nested-value");
	}
}
