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
@Profile("!test & !mssql-test & !testcontainer")
public class HaJdbcConfig {

    private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final int MINIMUM_IDLE = 5;
    private static final String CLUSTER_NAME = "sample";
    private static final String SYNCHRONIZATION_STRATEGY = "passive";
    private static final String H2_URL_PATTERN = "jdbc:h2:./ha-jdbc-state/{0}";
    private static final String H2_USER = "sa";
    private static final String FAILURE_DETECT_SCHEDULE = "0/10 * * * * ?";
    private static final String AUTO_ACTIVATE_SCHEDULE = "0/30 * * * * ?";

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

    private HikariDataSource createHikariDataSource(String url, String username, String password, String poolName) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(POSTGRESQL_DRIVER);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        dataSource.setMinimumIdle(MINIMUM_IDLE);
        dataSource.setPoolName(poolName);
        return dataSource;
    }

    @Bean
    public DataSource dataSource() {
        // Create master and slave HikariDataSources
        HikariDataSource masterDs = createHikariDataSource(masterUrl, masterUsername, masterPassword, "ha-jdbc-master");
        HikariDataSource slaveDs = createHikariDataSource(slaveUrl, slaveUsername, slavePassword, "ha-jdbc-slave");

        // Create HA-JDBC DataSource
        net.sf.hajdbc.sql.DataSource haJdbcDataSource = new net.sf.hajdbc.sql.DataSource();
        haJdbcDataSource.setCluster(CLUSTER_NAME);

        DataSourceDatabaseClusterConfigurationBuilder builder = haJdbcDataSource.getConfigurationBuilder();
        builder.addDatabase("master").dataSource(masterDs).weight(10);
        builder.addDatabase("slave").dataSource(slaveDs).weight(1);
        builder.addSynchronizationStrategy(SYNCHRONIZATION_STRATEGY);
        builder.defaultSynchronizationStrategy(SYNCHRONIZATION_STRATEGY);

        // Enable StateManager with H2 embedded database
        SQLStateManagerFactory stateManagerFactory = new SQLStateManagerFactory();
        stateManagerFactory.setUrlPattern(H2_URL_PATTERN);
        stateManagerFactory.setUser(H2_USER);
        stateManagerFactory.setPassword(statePassword);
        builder.state(stateManagerFactory);

        // Configure failure detection and auto-activation
        builder.failureDetectSchedule(FAILURE_DETECT_SCHEDULE);
        builder.autoActivateSchedule(AUTO_ACTIVATE_SCHEDULE);

        return haJdbcDataSource;
    }
}
