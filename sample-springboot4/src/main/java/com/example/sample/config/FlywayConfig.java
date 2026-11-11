package com.example.sample.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test & !mssql-test & !testcontainer & !mssql")
public class FlywayConfig {

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

    @Bean
    public CommandLineRunner flywayMigrate() {
        return args -> {
            // Migrate master database directly
            Flyway masterFlyway = Flyway.configure()
                    .dataSource(masterUrl, masterUsername, masterPassword)
                    .locations("classpath:db/migration")
                    .load();
            masterFlyway.migrate();

            // Migrate slave database directly
            Flyway slaveFlyway = Flyway.configure()
                    .dataSource(slaveUrl, slaveUsername, slavePassword)
                    .locations("classpath:db/migration")
                    .load();
            slaveFlyway.migrate();
        };
    }
}
