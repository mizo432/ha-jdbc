package com.example.sample.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import net.sf.hajdbc.sql.DataSourceDatabaseClusterConfigurationBuilder;
import net.sf.hajdbc.state.sql.SQLStateManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class HaJdbcConfig {

    @Value("${database.master.url}")
    private String masterUrl;

    @Value("${database.master.username}")
    private String masterUsername;

    @Value("${database.master.password}")
    private String masterPassword;

    @Value("${database.slave.url}")
    private String slaveUrl;

    @Value("${database.slave.username}")
    private String slaveUsername;

    @Value("${database.slave.password}")
    private String slavePassword;

    @Value("${database.state.password:}")
    private String statePassword;

    @Bean
    public DataSource dataSource() {
        // Create master HikariDataSource
        HikariDataSource masterDs = new HikariDataSource();
        masterDs.setDriverClassName("org.postgresql.Driver");
        masterDs.setJdbcUrl(masterUrl);
        masterDs.setUsername(masterUsername);
        masterDs.setPassword(masterPassword);
        masterDs.setMaximumPoolSize(10);
        masterDs.setMinimumIdle(5);
        masterDs.setPoolName("ha-jdbc-master");

        // Create slave HikariDataSource
        HikariDataSource slaveDs = new HikariDataSource();
        slaveDs.setDriverClassName("org.postgresql.Driver");
        slaveDs.setJdbcUrl(slaveUrl);
        slaveDs.setUsername(slaveUsername);
        slaveDs.setPassword(slavePassword);
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

        // Enable StateManager with H2 embedded database
        SQLStateManagerFactory stateManagerFactory = new SQLStateManagerFactory();
        stateManagerFactory.setUrlPattern("jdbc:h2:./ha-jdbc-state/{0}");
        stateManagerFactory.setUser("sa");
        stateManagerFactory.setPassword(statePassword);
        builder.state(stateManagerFactory);

        // Configure failure detection every 10 seconds
        builder.failureDetectSchedule("0/10 * * * * ?");

        // Configure auto-activation every 30 seconds
        builder.autoActivateSchedule("0/30 * * * * ?");

        return haJdbcDataSource;
    }
}
