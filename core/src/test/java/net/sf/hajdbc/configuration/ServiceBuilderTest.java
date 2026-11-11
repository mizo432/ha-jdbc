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
package net.sf.hajdbc.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.sf.hajdbc.Identifiable;

import org.junit.jupiter.api.Test;

public class ServiceBuilderTest
{
	@Test
	public void findDescriptorsWithSimpleBean()
	{
		// Test with a simple bean that has standard property editors
		java.util.Map<String, java.util.Map.Entry<java.beans.PropertyDescriptor, java.beans.PropertyEditor>> descriptors =
			ServiceBuilder.findDescriptors(TestBean.class);
		
		// String properties should have editors
		assertThat(descriptors).containsKey("name");
		assertThat(descriptors).containsKey("count");
	}
	
	@Test
	public void findDescriptorsExcludesClassProperty()
	{
		java.util.Map<String, java.util.Map.Entry<java.beans.PropertyDescriptor, java.beans.PropertyEditor>> descriptors =
			ServiceBuilder.findDescriptors(TestBean.class);
		
		// "class" is a special property that should not be included
		assertThat(descriptors).doesNotContainKey("class");
	}
	
	@Test
	public void findDescriptorsWithEmptyBean()
	{
		java.util.Map<String, java.util.Map.Entry<java.beans.PropertyDescriptor, java.beans.PropertyEditor>> descriptors =
			ServiceBuilder.findDescriptors(EmptyBean.class);
		
		assertThat(descriptors).isEmpty();
	}
	
	@Test
	public void findDescriptorsExcludesWriteOnlyProperties()
	{
		java.util.Map<String, java.util.Map.Entry<java.beans.PropertyDescriptor, java.beans.PropertyEditor>> descriptors =
			ServiceBuilder.findDescriptors(WriteOnlyBean.class);
		
		// writeOnly has no read method, so it should not be included
		assertThat(descriptors).doesNotContainKey("writeOnly");
	}
	
	@Test
	public void findDescriptorsExcludesReadOnlyProperties()
	{
		java.util.Map<String, java.util.Map.Entry<java.beans.PropertyDescriptor, java.beans.PropertyEditor>> descriptors =
			ServiceBuilder.findDescriptors(ReadOnlyBean.class);
		
		// readOnly has no write method, so it should not be included
		assertThat(descriptors).doesNotContainKey("readOnly");
	}
	
	// Test beans
	
	public static class TestBean implements Identifiable {
		private String name = "";
		private int count = 0;
		private String id = "test";
		
		@Override
		public String getId() { return id; }
		
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public int getCount() { return count; }
		public void setCount(int count) { this.count = count; }
	}
	
	public static class EmptyBean implements Identifiable {
		@Override
		public String getId() { return "empty"; }
	}
	
	public static class WriteOnlyBean implements Identifiable {
		private String writeOnly;
		
		@Override
		public String getId() { return "writeOnly"; }
		
		public void setWriteOnly(String writeOnly) { this.writeOnly = writeOnly; }
	}
	
	public static class ReadOnlyBean implements Identifiable {
		@Override
		public String getId() { return "readOnly"; }
		
		public String getReadOnly() { return "value"; }
	}
}
