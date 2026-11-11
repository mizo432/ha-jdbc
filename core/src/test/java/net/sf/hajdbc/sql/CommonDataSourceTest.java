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
package net.sf.hajdbc.sql;

import java.sql.SQLException;

import net.sf.hajdbc.DatabaseClusterConfigurationBuilder;
import net.sf.hajdbc.DatabaseClusterConfigurationFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link CommonDataSource}.
 */
public class CommonDataSourceTest
{
	/**
	 * Concrete test implementation of the abstract CommonDataSource class.
	 */
	private static class TestCommonDataSource extends CommonDataSource<javax.sql.DataSource, DataSourceDatabase, DataSourceDatabaseBuilder, CommonDataSourceProxyFactory<javax.sql.DataSource, DataSourceDatabase>>
	{
		@Override
		public CommonDataSourceProxyFactory<javax.sql.DataSource, DataSourceDatabase> createProxyFactory(
			net.sf.hajdbc.DatabaseCluster<javax.sql.DataSource, DataSourceDatabase> cluster)
		{
			return mock(CommonDataSourceProxyFactory.class);
		}

		@Override
		public DatabaseClusterConfigurationBuilder<javax.sql.DataSource, DataSourceDatabase, DataSourceDatabaseBuilder> getConfigurationBuilder()
		{
			return mock(DatabaseClusterConfigurationBuilder.class);
		}
	}

	@Test
	public void getClusterReturnsNullByDefault()
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		
		Assertions.assertNull(dataSource.getCluster());
	}
	
	@Test
	public void setAndGetCluster()
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		
		dataSource.setCluster("test-cluster");
		
		Assertions.assertEquals("test-cluster", dataSource.getCluster());
	}
	
	@Test
	public void getConfigReturnsNullByDefault()
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		
		Assertions.assertNull(dataSource.getConfig());
	}
	
	@Test
	public void setAndGetConfig()
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		
		dataSource.setConfig("test-config.xml");
		
		Assertions.assertEquals("test-config.xml", dataSource.getConfig());
	}
	
	@Test
	public void getUserReturnsNullByDefault()
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		
		Assertions.assertNull(dataSource.getUser());
	}
	
	@Test
	public void setAndGetUser()
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		
		dataSource.setUser("test-user");
		
		Assertions.assertEquals("test-user", dataSource.getUser());
	}
	
	@Test
	public void getPasswordReturnsNullByDefault()
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		
		Assertions.assertNull(dataSource.getPassword());
	}
	
	@Test
	public void setAndGetPassword()
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		
		dataSource.setPassword("test-password");
		
		Assertions.assertEquals("test-password", dataSource.getPassword());
	}
	
	@Test
	public void getConfigurationFactoryReturnsNullByDefault()
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		
		Assertions.assertNull(dataSource.getConfigurationFactory());
	}
	
	@Test
	public void setAndGetConfigurationFactory()
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		DatabaseClusterConfigurationFactory<javax.sql.DataSource, DataSourceDatabase> factory = mock(DatabaseClusterConfigurationFactory.class);
		
		dataSource.setConfigurationFactory(factory);
		
		Assertions.assertSame(factory, dataSource.getConfigurationFactory());
	}
	
	@Test
	public void getProxyThrowsWhenClusterNotSet() throws SQLException
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		
		SQLException exception = Assertions.assertThrows(SQLException.class, () -> dataSource.getProxy());
		Assertions.assertEquals("Cluster ID is not configured", exception.getMessage());
	}
	
	@Test
	public void closeDoesNotThrowWhenNotInitialized() throws SQLException
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		
		Assertions.assertDoesNotThrow(() -> dataSource.close());
	}
	
	@Test
	public void stopDelegatesToClose() throws SQLException
	{
		TestCommonDataSource dataSource = new TestCommonDataSource();
		
		// stop() should not throw when not initialized
		Assertions.assertDoesNotThrow(() -> dataSource.stop());
	}
}
