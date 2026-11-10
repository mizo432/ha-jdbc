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
package net.sf.hajdbc.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for SystemProperties utility class, particularly environment variable access.
 */
class SystemPropertiesTest {

    @Test
    void getSystemPropertyReturnsValue() {
        String original = SystemProperties.getSystemProperty("user.home");
        assertThat(original).isNotNull().isNotEmpty();
    }

    @Test
    void getSystemPropertyReturnsDefaultWhenMissing() {
        assertThat(SystemProperties.getSystemProperty("non.existent.property.12345", "fallback"))
                .isEqualTo("fallback");
    }

    @Test
    void getSystemPropertyReturnsNullWhenMissingAndNoDefault() {
        assertThat(SystemProperties.getSystemProperty("non.existent.property.12345"))
                .isNull();
    }

    @Test
    void getEnvironmentVariableReturnsNullWhenMissing() {
        assertThat(SystemProperties.getEnvironmentVariable("NON_EXISTENT_ENV_VAR_12345"))
                .isNull();
    }

    @Test
    void getEnvironmentVariableReturnsDefaultWhenMissing() {
        assertThat(SystemProperties.getEnvironmentVariable("NON_EXISTENT_ENV_VAR_12345", "fallback"))
                .isEqualTo("fallback");
    }

    @Test
    void getEnvironmentVariableReturnsValue() {
        // PATH is almost always set on any system
        String path = SystemProperties.getEnvironmentVariable("PATH");
        assertThat(path).isNotNull().isNotEmpty();
    }

    @Test
    void setAndGetSystemProperty() {
        String key = "test.hajdbc.temp.property";
        try {
            SystemProperties.setSystemProperty(key, "test-value");
            assertThat(SystemProperties.getSystemProperty(key)).isEqualTo("test-value");
        } finally {
            System.clearProperty(key);
        }
    }

    @Test
    void getSystemPropertiesReturnsNonEmpty() {
        assertThat(SystemProperties.getSystemProperties()).isNotNull().isNotEmpty();
    }
}
