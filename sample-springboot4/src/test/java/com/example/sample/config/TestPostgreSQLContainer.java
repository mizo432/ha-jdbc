package com.example.sample.config;

import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Testcontainers用のPostgreSQLコンテナを管理するクラス。
 */
public class TestPostgreSQLContainer {

    public static PostgreSQLContainer postgresMaster = new PostgreSQLContainer("postgres:17")
            .withDatabaseName("sample_db")
            .withUsername("test")
            .withPassword("test");

    public static PostgreSQLContainer postgresSlave = new PostgreSQLContainer("postgres:17")
            .withDatabaseName("sample_db")
            .withUsername("test")
            .withPassword("test");
}
