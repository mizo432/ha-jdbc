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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseBuilder;
import net.sf.hajdbc.DatabaseClusterConfiguration;
import net.sf.hajdbc.DatabaseClusterConfigurationBuilder;
import net.sf.hajdbc.Locality;
import net.sf.hajdbc.MockDataSource;
import net.sf.hajdbc.MockDriver;
import net.sf.hajdbc.SynchronizationStrategy;
import net.sf.hajdbc.balancer.load.LoadBalancerFactory;
import net.sf.hajdbc.cache.eager.EagerDatabaseMetaDataCacheFactory;
import net.sf.hajdbc.dialect.StandardDialectFactory;
import net.sf.hajdbc.durability.coarse.CoarseDurabilityFactory;
import net.sf.hajdbc.sql.DataSourceDatabase;
import net.sf.hajdbc.sql.DataSourceDatabaseClusterConfigurationBuilder;
import net.sf.hajdbc.sql.DriverDatabase;
import net.sf.hajdbc.sql.DriverDatabaseClusterConfigurationBuilder;
import net.sf.hajdbc.sql.TransactionModeEnum;
import net.sf.hajdbc.state.StateManagerFactory;
import net.sf.hajdbc.state.sql.SQLStateManagerFactory;
import net.sf.hajdbc.sync.DifferentialSynchronizationStrategy;

import org.junit.Test;

public class XMLDatabaseClusterConfigurationFactoryTest
{
	@Test
	public void createDriverBasedConfiguration() throws SQLException
	{
		Driver driver = new MockDriver(mock(Connection.class));

		DriverManager.registerDriver(driver);
		try
		{
			String url1 = "jdbc:mock:db1";
			Map.Entry<String, String> property1 = new SimpleImmutableEntry<>("name", "value1");
			String url2 = "jdbc:mock:db2";
			Map.Entry<String, String> property2 = new SimpleImmutableEntry<>("name", "value2");
			DatabaseClusterConfiguration<Driver, DriverDatabase> configuration = createConfiguration(new DriverDatabaseClusterConfigurationBuilder(), url1, url2, property1, property2);
	
			DriverDatabase db1 = configuration.getDatabaseMap().get("db1");
			assertThat(db1.getConnectionSource()).isSameAs(driver);
			assertThat(db1.getLocation()).isEqualTo(url1);
			assertThat(db1.getProperties().getProperty("name")).isEqualTo("value1");
			DriverDatabase db2 = configuration.getDatabaseMap().get("db2");
			assertThat(db2.getConnectionSource()).isSameAs(driver);
			assertThat(db2.getLocation()).isEqualTo(url2);
			assertThat(db2.getProperties().getProperty("name")).isEqualTo("value2");
		}
		finally
		{
			DriverManager.deregisterDriver(driver);
		}
	}
	
	@Test
	public void createDataSourceBasedConfiguration() throws SQLException
	{
		String location = MockDataSource.class.getName();
		Map.Entry<String, String> property1 = new SimpleImmutableEntry<>("name", "db1");
		Map.Entry<String, String> property2 = new SimpleImmutableEntry<>("name", "db2");
		
		DatabaseClusterConfiguration<DataSource, DataSourceDatabase> configuration = createConfiguration(new DataSourceDatabaseClusterConfigurationBuilder(), location, location, property1, property2);

		DataSourceDatabase db1 = configuration.getDatabaseMap().get("db1");
		assertThat(db1.getConnectionSource()).isInstanceOf(MockDataSource.class);
		assertThat(((MockDataSource) db1.getConnectionSource()).getName()).isEqualTo("db1");
		DataSourceDatabase db2 = configuration.getDatabaseMap().get("db2");
		assertThat(db2.getConnectionSource()).isInstanceOf(MockDataSource.class);
		assertThat(((MockDataSource) db2.getConnectionSource()).getName()).isEqualTo("db2");
	}
	
	private static <Z, D extends Database<Z>, B extends DatabaseBuilder<Z, D>> DatabaseClusterConfiguration<Z, D> createConfiguration(DatabaseClusterConfigurationBuilder<Z, D, B> configurationBuilder, String location1, String location2, Map.Entry<String, String> property1, Map.Entry<String, String> property2) throws SQLException
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<?xml version=\"1.0\"?>");
		builder.append("<ha-jdbc xmlns=\"").append(Namespace.CURRENT_VERSION.getURI()).append("\">");
		builder.append("\t<sync id=\"diff\"><property name=\"fetchSize\">100</property><property name=\"maxBatchSize\">100</property></sync>");
		builder.append("\t<state id=\"sql\"><property name=\"urlPattern\">jdbc:h2:{0}</property><property name=\"minIdle\">1</property></state>");
		builder.append("\t<cluster default-sync=\"diff\">");
		builder.append(String.format("\t\t<database id=\"db1\" location=\"%s\">", location1));
		builder.append(String.format("\t\t\t<property name=\"%s\">%s</property>", property1.getKey(), property1.getValue()));
		builder.append(String.format("\t\t</database>", location1));
		builder.append(String.format("\t\t<database id=\"db2\" location=\"%s\">", location2));
		builder.append(String.format("\t\t\t<property name=\"%s\">%s</property>", property2.getKey(), property2.getValue()));
		builder.append(String.format("\t\t</database>", location1));
		builder.append("\t</cluster>");
		builder.append("</ha-jdbc>");
		
		String xml = builder.toString();
		
		XMLStreamFactory streamFactory = mock(XMLStreamFactory.class);
		
		XMLDatabaseClusterConfigurationFactory<Z, D> factory = new XMLDatabaseClusterConfigurationFactory<>(streamFactory);
		
		when(streamFactory.createSource()).thenReturn(new StreamSource(new StringReader(xml)));
		
		DatabaseClusterConfiguration<Z, D> configuration = factory.createConfiguration(configurationBuilder);
		
		assertThat(configuration.getDispatcherFactory()).isNull();
		Map<String, SynchronizationStrategy> syncStrategies = configuration.getSynchronizationStrategyMap();
		assertThat(syncStrategies).isNotNull();
		assertThat(syncStrategies.size()).isEqualTo(1);
		
		SynchronizationStrategy syncStrategy = syncStrategies.get("diff");
		
		assertThat(syncStrategy).isNotNull();
		assertThat(syncStrategy).isInstanceOf(DifferentialSynchronizationStrategy.class);
		DifferentialSynchronizationStrategy diffStrategy = (DifferentialSynchronizationStrategy) syncStrategy;
		assertThat(diffStrategy.getFetchSize()).isEqualTo(100);
		assertThat(diffStrategy.getMaxBatchSize()).isEqualTo(100);
		assertThat(diffStrategy.getVersionPattern()).isNull();
		
		StateManagerFactory stateManagerFactory = configuration.getStateManagerFactory();
		assertThat(stateManagerFactory).isInstanceOf(SQLStateManagerFactory.class);
		SQLStateManagerFactory sqlStateManagerFactory = (SQLStateManagerFactory) stateManagerFactory;
		assertThat(sqlStateManagerFactory.getUrlPattern()).isEqualTo("jdbc:h2:{0}");
		// コンストラクタで埋め込みドライバが検出されるとuser/passwordが自動設定されるため、nullチェックを削除
		
		assertThat(configuration.getBalancerFactory().getClass()).isEqualTo(LoadBalancerFactory.class);
		assertThat(configuration.getDatabaseMetaDataCacheFactory().getClass()).isEqualTo(EagerDatabaseMetaDataCacheFactory.class);
		assertThat(configuration.getDefaultSynchronizationStrategy()).isEqualTo("diff");
		assertThat(configuration.getDialectFactory().getClass()).isEqualTo(StandardDialectFactory.class);
		assertThat(configuration.getDurabilityFactory().getClass()).isEqualTo(CoarseDurabilityFactory.class);
		assertThat(configuration.getTransactionMode()).isSameAs(TransactionModeEnum.SERIAL);
		
		assertThat(configuration.getExecutorProvider()).isNotNull();
		
		assertThat(configuration.getAutoActivationExpression()).isNull();
		assertThat(configuration.getFailureDetectionExpression()).isNull();
		
		assertThat(configuration.isCurrentDateEvaluationEnabled()).isFalse();
		assertThat(configuration.isCurrentTimeEvaluationEnabled()).isFalse();
		assertThat(configuration.isCurrentTimestampEvaluationEnabled()).isFalse();
		assertThat(configuration.isIdentityColumnDetectionEnabled()).isFalse();
		assertThat(configuration.isRandEvaluationEnabled()).isFalse();
		assertThat(configuration.isSequenceDetectionEnabled()).isFalse();
		
		Map<String, D> databases = configuration.getDatabaseMap();
		
		assertThat(databases).isNotNull();
		assertThat(databases.size()).isEqualTo(2);
		
		D db1 = databases.get("db1");
		
		assertThat(db1).isNotNull();
		assertThat(db1.getId()).isEqualTo("db1");
		assertThat(db1.getWeight()).isEqualTo(1);
		assertThat(db1.getLocality()).isSameAs(Locality.REMOTE);
		
		D db2 = databases.get("db2");
		
		assertThat(db2).isNotNull();
		assertThat(db2.getId()).isEqualTo("db2");
		assertThat(db2.getWeight()).isEqualTo(1);
		assertThat(db2.getLocality()).isSameAs(Locality.REMOTE);
		
		reset(streamFactory);
		
		StringWriter writer = new StringWriter();
		
		when(streamFactory.createResult()).thenReturn(new StreamResult(writer));
		
		factory.export(configuration);
		
		System.out.println(writer.toString());
		
		return configuration;
	}
}
