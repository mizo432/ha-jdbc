package com.example.sample.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import net.sf.hajdbc.DatabaseClusterConfiguration;
import net.sf.hajdbc.DatabaseClusterConfigurationBuilder;
import net.sf.hajdbc.SimpleDatabaseClusterConfigurationFactory;
import net.sf.hajdbc.sql.DataSourceDatabase;
import net.sf.hajdbc.sql.DataSourceDatabaseBuilder;
import net.sf.hajdbc.sql.DataSourceDatabaseClusterConfigurationBuilder;
import net.sf.hajdbc.sync.PassiveSynchronizationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class HaJdbcConfig {

    /**
     * Custom DataSource that configures HA-JDBC programmatically.
     * Sets configurationFactory in constructor to prevent XMLDatabaseClusterConfigurationFactory from being used.
     */
    private static class HaJdbcDataSource extends net.sf.hajdbc.sql.DataSource {
        private final DataSourceDatabaseClusterConfigurationBuilder builder = new DataSourceDatabaseClusterConfigurationBuilder();

        HaJdbcDataSource() {
            // Configure the builder directly
            builder.addSynchronizationStrategy(new PassiveSynchronizationStrategy());
            builder.defaultSynchronizationStrategy("passive");

            // Configure master database
            DataSourceDatabaseBuilder masterBuilder = builder.addDatabase("master");
            masterBuilder.location("com.zaxxer.hikari.HikariDataSource");
            masterBuilder.credentials("sample_user", "sample_password");
            masterBuilder.property("driverClassName", "org.postgresql.Driver");
            masterBuilder.property("jdbcUrl", "jdbc:postgresql://localhost:5432/sample_db");
            masterBuilder.property("maximumPoolSize", "10");
            masterBuilder.property("minimumIdle", "5");
            masterBuilder.weight(10);

            // Configure slave database
            DataSourceDatabaseBuilder slaveBuilder = builder.addDatabase("slave");
            slaveBuilder.location("com.zaxxer.hikari.HikariDataSource");
            slaveBuilder.credentials("sample_user", "sample_password");
            slaveBuilder.property("driverClassName", "org.postgresql.Driver");
            slaveBuilder.property("jdbcUrl", "jdbc:postgresql://localhost:5433/sample_db");
            slaveBuilder.property("maximumPoolSize", "10");
            slaveBuilder.property("minimumIdle", "5");
            slaveBuilder.weight(1);

            // Create a factory that uses our pre-configured builder
            SimpleDatabaseClusterConfigurationFactory<javax.sql.DataSource, DataSourceDatabase> configFactory = 
                new SimpleDatabaseClusterConfigurationFactory<javax.sql.DataSource, DataSourceDatabase>() {
                @Override
                public <B extends net.sf.hajdbc.DatabaseBuilder<javax.sql.DataSource, DataSourceDatabase>>
                DatabaseClusterConfiguration<javax.sql.DataSource, DataSourceDatabase> createConfiguration(
                        DatabaseClusterConfigurationBuilder<javax.sql.DataSource, DataSourceDatabase, B> b) throws SQLException {
                    // Use our pre-configured builder instead of the passed-in builder
                    return builder.build();
                }
            };
            
            // Set configurationFactory in constructor to prevent XMLDatabaseClusterConfigurationFactory from being used
            setConfigurationFactory(configFactory);
        }

        @Override
        public DataSourceDatabaseClusterConfigurationBuilder getConfigurationBuilder() {
            return this.builder;
        }
    }

    @Bean
    public DataSource dataSource() {
        HaJdbcDataSource haJdbcDataSource = new HaJdbcDataSource();
        haJdbcDataSource.setCluster("sample");
        return haJdbcDataSource;
    }
}
