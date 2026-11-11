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
package net.sf.hajdbc.sync;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseProperties;
import net.sf.hajdbc.codec.Decoder;
import net.sf.hajdbc.dialect.Dialect;


/**
 * @author Paul Ferraro
 * @param <D> Driver or DataSource
 * @since 2.0
 */
public interface SynchronizationContext<Z, D extends Database<Z>> extends AutoCloseable
{
	/**
	 * Returns a connection to the specified database.
	 * @param database the database to connect to
	 * @return database connection
	 * @throws SQLException if the connection could not be obtained
	 */
	Connection getConnection(D database) throws SQLException;
	
	/**
	 * Returns the source database to synchronize from.
	 * @return database
	 */
	D getSourceDatabase();
	
	/**
	 * Returns the target database to synchronize to.
	 * @return database
	 */
	D getTargetDatabase();
	
	/**
	 * Returns a snapshot of the cluster's active databases at the time synchronization started.
	 * @return collection of databases
	 */
	Set<D> getActiveDatabaseSet();
	
	/**
	 * Returns the database metadata cache for the source database.
	 * @return database metadata cache
	 */
	DatabaseProperties getSourceDatabaseProperties();
	
	/**
	 * Returns the database metadata cache for the target database.
	 * @return database metadata cache
	 */
	DatabaseProperties getTargetDatabaseProperties();
	
	/**
	 * Returns the Dialect of the cluster.
	 * @return Dialect
	 */
	Dialect getDialect();
	
	/**
	 * Returns the executor service for executing tasks asynchronously.
	 * @return executor service
	 */
	ExecutorService getExecutor();
	
	SynchronizationSupport getSynchronizationSupport();
	
	Decoder getDecoder();
	
	/**
	 * Closes any open database connections and shuts down the executor service. 
	 */
	@Override
	void close();
}
