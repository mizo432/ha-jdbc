package com.example.sample.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import net.sf.hajdbc.sql.DataSourceDatabaseClusterConfigurationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class HaJdbcConfig {

    @Bean
    public DataSource dataSource() {
        // Create master HikariDataSource
        HikariDataSource masterDs = new HikariDataSource();
        masterDs.setDriverClassName("org.postgresql.Driver");
        masterDs.setJdbcUrl("jdbc:postgresql://localhost:5432/sample_db");
        masterDs.setUsername("sample_user");
        masterDs.setPassword("sample_password");
        masterDs.setMaximumPoolSize(10);
        masterDs.setMinimumIdle(5);
        masterDs.setPoolName("ha-jdbc-master");

        // Create slave HikariDataSource
        HikariDataSource slaveDs = new HikariDataSource();
        slaveDs.setDriverClassName("org.postgresql.Driver");
        slaveDs.setJdbcUrl("jdbc:postgresql://localhost:5433/sample_db");
        slaveDs.setUsername("sample_user");
        slaveDs.setPassword("sample_password");
        slaveDs.setMaximumPoolSize(10);
        slaveDs.setMinimumIdle(5);
        slaveDs.setPoolName("ha-jdbc-slave");

        // Create HA-JDBC DataSource
        net.sf.hajdbc.sql.DataSource haJdbcDataSource = new net.sf.hajdbc.sql.DataSource();
        haJdbcDataSource.setCluster("sample");
        
        DataSourceDatabaseClusterConfigurationBuilder builder = haJdbcDataSource.getConfigurationBuilder();
        builder.addDatabase("master").dataSource(masterDs).weight(10);
        builder.addDatabase("slave").dataSource(slaveDs).weight(1);
        builder.addSynchronizationStrategy("passive");
        builder.defaultSynchronizationStrategy("passive");
        
        return haJdbcDataSource;
    }
}
