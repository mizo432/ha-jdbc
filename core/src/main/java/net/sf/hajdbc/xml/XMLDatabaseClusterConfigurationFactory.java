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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlMapper;

import javax.xml.stream.XMLInputFactory;

import net.sf.hajdbc.Credentials;
import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseBuilder;
import net.sf.hajdbc.DatabaseClusterConfiguration;
import net.sf.hajdbc.DatabaseClusterConfigurationBuilder;
import net.sf.hajdbc.DatabaseClusterConfigurationFactory;
import net.sf.hajdbc.Locality;
import net.sf.hajdbc.SynchronizationStrategy;
import net.sf.hajdbc.Version;
import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.messages.Messages;
import net.sf.hajdbc.messages.MessagesFactory;
import net.sf.hajdbc.sql.TransactionModeEnum;
import net.sf.hajdbc.util.SystemProperties;

/**
 * {@link DatabaseClusterConfigurationFactory} that parses an xml configuration file using Jackson 3 XML.
 * @author Paul Ferraro
 */
public class XMLDatabaseClusterConfigurationFactory<Z, D extends Database<Z>> implements DatabaseClusterConfigurationFactory<Z, D>, Constants
{
	private static final long serialVersionUID = -8796872297122349961L;
	
	private static final String CONFIG_PROPERTY_FORMAT = "ha-jdbc.{0}.configuration";
	private static final String CONFIG_PROPERTY = "ha-jdbc.configuration";
	private static final String DEFAULT_RESOURCE = "ha-jdbc-{0}.xml";

	private static final Logger logger = LoggerFactory.getLogger(XMLDatabaseClusterConfigurationFactory.class);
	private static final Messages messages = MessagesFactory.getMessages();

	private final XmlMapper xmlMapper;
	private final XMLStreamFactory streamFactory;
	private final Map<String, Namespace> namespaces = new HashMap<>();

	private static String identifyResource(String id)
	{
		String resource = SystemProperties.getSystemProperty(MessageFormat.format(CONFIG_PROPERTY_FORMAT, id));
		
		return (resource != null) ? resource : MessageFormat.format(SystemProperties.getSystemProperty(CONFIG_PROPERTY, DEFAULT_RESOURCE), id);
	}
	
	/**
	 * Algorithm for searching class loaders for HA-JDBC url.
	 * @param resource a resource name
	 * @return a URL for the HA-JDBC configuration resource
	 */
	private static URL findResource(String resource, ClassLoader loader)
	{
		try
		{
			return new URL(resource);
		}
		catch (MalformedURLException e)
		{
			return findResource(resource, loader, XMLDatabaseClusterConfigurationFactory.class.getClassLoader(), ClassLoader.getSystemClassLoader());
		}
	}

	private static URL findResource(String resource, ClassLoader... loaders)
	{
		if (loaders.length == 0) return findResource(resource, Thread.currentThread().getContextClassLoader());
		
		for (ClassLoader loader: loaders)
		{
			if (loader != null)
			{
				URL url = loader.getResource(resource);
				
				if (url != null) return url;
			}
		}
		throw new IllegalArgumentException("Resource not found: " + resource);
	}

	private static XmlMapper createXmlMapper()
	{
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		XmlFactory xmlFactory = XmlFactory.builder().xmlInputFactory(inputFactory).build();
		return XmlMapper.builder(xmlFactory).build();
	}

	public XMLDatabaseClusterConfigurationFactory(String id, String resource)
	{
		this(id, resource, XMLDatabaseClusterConfigurationFactory.class.getClassLoader());
	}
	
	public XMLDatabaseClusterConfigurationFactory(String id, String resource, ClassLoader loader)
	{
		this(findResource((resource == null) ? identifyResource(id) : MessageFormat.format(resource, id), loader));
	}
	
	public XMLDatabaseClusterConfigurationFactory(URL url)
	{
		this(url.getProtocol().equals("file") ? new FileXMLStreamFactory(url) : new URLXMLStreamFactory(url));
	}
	
	public XMLDatabaseClusterConfigurationFactory(XMLStreamFactory streamFactory)
	{
		this(createXmlMapper(), streamFactory);
		for (Namespace namespace: Namespace.values())
		{
			this.namespaces.put(namespace.getURI(), namespace);
		}
	}

	public XMLDatabaseClusterConfigurationFactory(XmlMapper xmlMapper, XMLStreamFactory streamFactory)
	{
		this.xmlMapper = xmlMapper;
		this.streamFactory = streamFactory;
		for (Namespace namespace: Namespace.values())
		{
			this.namespaces.put(namespace.getURI(), namespace);
		}
	}
	
	@Override
	public <B extends DatabaseBuilder<Z, D>> DatabaseClusterConfiguration<Z, D> createConfiguration(DatabaseClusterConfigurationBuilder<Z, D, B> builder) throws SQLException
	{
		logger.log(Level.INFO, messages.init(Version.CURRENT, this.streamFactory));
		
		try (InputStream is = streamFactory.createInputStream())
		{
			// Read all bytes from input stream
			byte[] bytes = is.readAllBytes();
			// Apply property replacement on the raw XML content
			String xmlContent = replaceProperties(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
			
			// Parse XML using Jackson
			Configuration config = this.xmlMapper.readValue(xmlContent, Configuration.class);
			
			// Validate namespace
			String uri = config.getNamespace();
			Namespace namespace = this.namespaces.get(uri);
			
			if (namespace == null)
			{
				throw new SQLException(messages.unsupportedNamespace());
			}
			
			// Build configuration from POJO
			this.buildConfiguration(config, builder);
			return builder.build();
		}
		catch (IOException e)
		{
			throw new SQLException(e);
		}
	}

	private String replaceProperties(String xmlContent)
	{
		return PropertyReplacementFilter.replace(xmlContent);
	}

	private <B extends DatabaseBuilder<Z, D>> void buildConfiguration(Configuration config, DatabaseClusterConfigurationBuilder<Z, D, B> builder)
	{
		// Distributable
		if (config.getDistributable() != null)
		{
			String id = config.getDistributable().getId() != null ? config.getDistributable().getId() : "jgroups";
			net.sf.hajdbc.configuration.ServiceBuilder<net.sf.hajdbc.distributed.CommandDispatcherFactory> distBuilder = builder.distributable(id);
			for (Property p : config.getDistributable().getProperty())
			{
				distBuilder.property(p.getName(), p.getValue());
			}
		}

		// Sync strategies
		for (IdentifiedDescriptor sync : config.getSync())
		{
			net.sf.hajdbc.configuration.ServiceBuilder<SynchronizationStrategy> syncBuilder = builder.addSynchronizationStrategy(sync.getId());
			for (Property p : sync.getProperty())
			{
				syncBuilder.property(p.getName(), p.getValue());
			}
		}

		// State manager
		if (config.getState() != null)
		{
			String id = config.getState().getId() != null ? config.getState().getId() : "sql";
			net.sf.hajdbc.configuration.ServiceBuilder<net.sf.hajdbc.state.StateManagerFactory> stateBuilder = builder.state(id);
			for (Property p : config.getState().getProperty())
			{
				stateBuilder.property(p.getName(), p.getValue());
			}
		}

		// Lock manager
		if (config.getLock() != null)
		{
			String id = config.getLock().getId() != null ? config.getLock().getId() : "semaphore";
			net.sf.hajdbc.configuration.ServiceBuilder<net.sf.hajdbc.lock.LockManagerFactory> lockBuilder = builder.lock(id);
			for (Property p : config.getLock().getProperty())
			{
				lockBuilder.property(p.getName(), p.getValue());
			}
		}

		// Cluster
		Cluster cluster = config.getCluster();
		if (cluster != null)
		{
			if (cluster.getDefaultSync() != null)
			{
				builder.defaultSynchronizationStrategy(cluster.getDefaultSync());
			}
			if (cluster.getBalancer() != null)
			{
				builder.balancer(cluster.getBalancer());
			}
			if (cluster.getMetaDataCache() != null)
			{
				builder.metaDataCache(cluster.getMetaDataCache());
			}
			if (cluster.getDialect() != null)
			{
				builder.dialect(cluster.getDialect());
			}
			if (cluster.getDurability() != null)
			{
				builder.durability(cluster.getDurability());
			}
			if (cluster.getInputSink() != null)
			{
				builder.inputSink(cluster.getInputSink());
			}
			if (cluster.getTransactionMode() != null)
			{
				builder.transactionMode(TransactionModeEnum.valueOf(cluster.getTransactionMode().toUpperCase(Locale.ENGLISH)));
			}
			if (cluster.getAutoActivateSchedule() != null)
			{
				builder.autoActivateSchedule(cluster.getAutoActivateSchedule());
			}
			if (cluster.getFailureDetectSchedule() != null)
			{
				builder.failureDetectSchedule(cluster.getFailureDetectSchedule());
			}
			if (cluster.getEvalCurrentDate() != null)
			{
				builder.evalCurrentDate(cluster.getEvalCurrentDate());
			}
			if (cluster.getEvalCurrentTime() != null)
			{
				builder.evalCurrentTime(cluster.getEvalCurrentTime());
			}
			if (cluster.getEvalCurrentTimestamp() != null)
			{
				builder.evalCurrentTimestamp(cluster.getEvalCurrentTimestamp());
			}
			if (cluster.getEvalRand() != null)
			{
				builder.evalRand(cluster.getEvalRand());
			}
			if (cluster.getDetectIdentityColumns() != null)
			{
				builder.detectIdentityColumns(cluster.getDetectIdentityColumns());
			}
			if (cluster.getDetectSequences() != null)
			{
				builder.detectSequences(cluster.getDetectSequences());
			}
			if (cluster.getAllowEmptyCluster() != null)
			{
				builder.allowEmptyCluster(cluster.getAllowEmptyCluster());
			}

			// Databases
			for (XmlDatabase db : cluster.getDatabase())
			{
				B dbBuilder = builder.addDatabase(db.getId());
				if (db.getLocation() != null)
				{
					dbBuilder.location(db.getLocation());
				}
				if (db.getWeight() != null)
				{
					dbBuilder.weight(db.getWeight());
				}
				if (db.getLocality() != null)
				{
					dbBuilder.locality(Locality.valueOf(db.getLocality().toUpperCase(Locale.US)));
				}
				if (db.getUser() != null)
				{
					dbBuilder.credentials(db.getUser(), db.getPassword());
				}
				for (Property p : db.getProperty())
				{
					dbBuilder.property(p.getName(), p.getValue());
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseClusterConfigurationListener#added(net.sf.hajdbc.Database, net.sf.hajdbc.DatabaseClusterConfiguration)
	 */
	@Override
	public void added(D database, DatabaseClusterConfiguration<Z, D> configuration)
	{
		this.export(configuration);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseClusterConfigurationListener#removed(net.sf.hajdbc.Database, net.sf.hajdbc.DatabaseClusterConfiguration)
	 */
	@Override
	public void removed(D database, DatabaseClusterConfiguration<Z, D> configuration)
	{
		this.export(configuration);
	}
	
	public void export(DatabaseClusterConfiguration<Z, D> configuration)
	{
		try (OutputStream os = streamFactory.createOutputStream())
		{
			Configuration config = this.toConfiguration(configuration);
			this.xmlMapper.writeValue(os, config);
		}
		catch (IOException e)
		{
			logger.log(Level.WARN, e);
		}
	}

	private Configuration toConfiguration(DatabaseClusterConfiguration<Z, D> config)
	{
		Configuration result = new Configuration();
		
		// Distributable
		if (config.getDispatcherFactory() != null)
		{
			IdentifiableDescriptor dist = new IdentifiableDescriptor();
			dist.setId(config.getDispatcherFactory().getId());
			result.setDistributable(dist);
		}
		
		// Sync strategies
		for (SynchronizationStrategy strategy : config.getSynchronizationStrategyMap().values())
		{
			IdentifiedDescriptor sync = new IdentifiedDescriptor();
			sync.setId(strategy.getId());
			result.getSync().add(sync);
		}
		
		// State manager
		if (config.getStateManagerFactory() != null)
		{
			IdentifiableDescriptor state = new IdentifiableDescriptor();
			state.setId(config.getStateManagerFactory().getId());
			result.setState(state);
		}
		
		// Lock manager
		if (config.getLockManagerFactory() != null)
		{
			IdentifiableDescriptor lock = new IdentifiableDescriptor();
			lock.setId(config.getLockManagerFactory().getId());
			result.setLock(lock);
		}
		
		// Cluster
		Cluster cluster = new Cluster();
		cluster.setDefaultSync(config.getDefaultSynchronizationStrategy());
		if (config.getBalancerFactory() != null)
		{
			cluster.setBalancer(config.getBalancerFactory().getId());
		}
		if (config.getDatabaseMetaDataCacheFactory() != null)
		{
			cluster.setMetaDataCache(config.getDatabaseMetaDataCacheFactory().getId());
		}
		if (config.getDialectFactory() != null)
		{
			cluster.setDialect(config.getDialectFactory().getId());
		}
		if (config.getDurabilityFactory() != null)
		{
			cluster.setDurability(config.getDurabilityFactory().getId());
		}
		if (config.getInputSinkProvider() != null)
		{
			cluster.setInputSink(config.getInputSinkProvider().getId());
		}
		if (config.getTransactionMode() != null)
		{
			cluster.setTransactionMode(config.getTransactionMode().toString().toLowerCase(Locale.ENGLISH));
		}
		if (config.getAutoActivationExpression() != null)
		{
			cluster.setAutoActivateSchedule(config.getAutoActivationExpression().toString());
		}
		if (config.getFailureDetectionExpression() != null)
		{
			cluster.setFailureDetectSchedule(config.getFailureDetectionExpression().toString());
		}
		cluster.setEvalCurrentDate(config.isCurrentDateEvaluationEnabled());
		cluster.setEvalCurrentTime(config.isCurrentTimeEvaluationEnabled());
		cluster.setEvalCurrentTimestamp(config.isCurrentTimestampEvaluationEnabled());
		cluster.setEvalRand(config.isRandEvaluationEnabled());
		cluster.setDetectIdentityColumns(config.isIdentityColumnDetectionEnabled());
		cluster.setDetectSequences(config.isSequenceDetectionEnabled());
		cluster.setAllowEmptyCluster(config.isEmptyClusterAllowed());
		
		for (D database : config.getDatabaseMap().values())
		{
			XmlDatabase db = new XmlDatabase();
			db.setId(database.getId());
			db.setLocation(database.getLocation());
			db.setWeight(database.getWeight());
			if (database.getLocality() != null)
			{
				db.setLocality(database.getLocality().toString().toLowerCase(Locale.US));
			}
			Credentials credentials = database.getCredentials();
			if (credentials != null)
			{
				db.setUser(credentials.getUser());
				db.setPassword(credentials.getEncodedPassword());
			}
			for (Map.Entry<Object, Object> entry : database.getProperties().entrySet())
			{
				Property p = new Property();
				p.setName(entry.getKey().toString());
				p.setValue(entry.getValue().toString());
				db.getProperty().add(p);
			}
			cluster.getDatabase().add(db);
		}
		
		result.setCluster(cluster);
		return result;
	}
}
