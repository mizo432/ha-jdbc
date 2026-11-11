# HA-JDBC: High-Availability JDBC

<http://ha-jdbc.github.com>

## Logging

HA-JDBC provides a logging abstraction that supports multiple logging frameworks through the ServiceLoader mechanism.
The following logging providers are available:

| Provider | Artifact | Description |
|----------|----------|-------------|
| SLF4J | `:logging:slf4j` | SLF4J-based logging (recommended for Spring Boot) |
| Commons Logging | `:logging:commons` | Apache Commons Logging |
| JDK Logging | Built-in | Java Util Logging (default fallback) |

### Including a Logging Provider

Add the desired logging provider as a dependency alongside the core module:

**Gradle (Kotlin DSL)**
```kotlin
dependencies {
    implementation(project(":core"))
    implementation(project(":logging:slf4j"))  // SLF4J provider
}
```

**Maven**
```xml
<dependency>
    <groupId>net.sf.hajdbc</groupId>
    <artifactId>ha-jdbc-core</artifactId>
    <version>${ha-jdbc.version}</version>
</dependency>
<dependency>
    <groupId>net.sf.hajdbc</groupId>
    <artifactId>ha-jdbc-logging-slf4j</artifactId>
    <version>${ha-jdbc.version}</version>
</dependency>
```

If multiple providers are on the classpath, the first one discovered by ServiceLoader is used.

### Log Levels

HA-JDBC supports the following log levels: `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE`.

### Production-Ready Logging Configuration

For production environments, it is recommended to set the following log levels to balance observability and performance:

**Spring Boot (`application.yml`)**
```yaml
logging:
  level:
    root: WARN
    net.sf.hajdbc: INFO
    org.hibernate.SQL: WARN
```

| Level | Package | Recommendation |
|-------|---------|----------------|
| `WARN` | `root` | Suppress verbose framework logs |
| `INFO` | `net.sf.hajdbc` | Capture HA-JDBC state transitions, failover events, and synchronization status |
| `WARN` | `org.hibernate.SQL` | Reduce SQL statement noise while still seeing problematic queries |

#### When to Adjust Log Levels

- **Troubleshooting failover issues**: Set `net.sf.hajdbc` to `DEBUG` to see detailed database health check results and connection state changes.
- **Performance investigation**: Set `net.sf.hajdbc` to `TRACE` to capture every JDBC call routed through HA-JDBC.
- **Audit logging**: Keep `net.sf.hajdbc` at `INFO` in production to record failover and synchronization events for operational visibility.

### Sample Log Output

This section provides sample log output for common operational scenarios, helping you monitor HA-JDBC behavior in production.

#### Database Failure (Deactivation)

When a database becomes unreachable, HA-JDBC detects the failure and deactivates it from the cluster. The following log sample shows a replica database (`slave`) going down due to a connection refusal:

```
2024-01-15 10:23:45.123 ERROR [hajdbc-failure-detection-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterFailureDetectionTask - Deactivated database 'slave' from cluster 'mycluster'
java.sql.SQLException: Connection refused
	at org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(ConnectionFactoryImpl.java:302)
	at org.postgresql.core.ConnectionFactory.openConnection(ConnectionFactory.java:51)
	at org.postgresql.jdbc.PgConnection.<init>(PgConnection.java:264)
	...
```

When a database fails during an active write operation (e.g., `InvokeOnManyInvocationStrategy` for write operations that target all active databases), the log includes the exception that triggered deactivation:

```
2024-01-15 10:23:45.456 ERROR [http-nio-8080-exec-5] net.sf.hajdbc.invocation.InvokeOnManyInvocationStrategy - Deactivated database 'slave' from cluster 'mycluster'
java.sql.SQLException: Connection refused
	at org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(ConnectionFactoryImpl.java:302)
	...
```

If multiple databases fail and their exceptions differ (inconsistent failure), HA-JDBC logs the inconsistency:

```
2024-01-15 10:23:45.789 ERROR [http-nio-8080-exec-5] net.sf.hajdbc.invocation.InvokeOnManyInvocationStrategy - Deactivated database 'slave' from cluster 'mycluster' due to inconsistent operations results: expected [java.sql.SQLException: Connection refused], actual [java.sql.SQLTransientConnectionException: HA100 - Unable to get managed connection]
java.sql.SQLTransientConnectionException: HA100 - Unable to get managed connection
	...
```

When all active databases in the cluster become unavailable, HA-JDBC throws an exception to the application:

```
2024-01-15 10:24:00.012 ERROR [http-nio-8080-exec-3] net.sf.hajdbc.invocation.InvokeOnOneInvocationStrategy - Database cluster 'mycluster' has no active databases
```

#### Database Recovery (Re-activation)

When a deactivated database becomes available again, HA-JDBC automatically detects it and re-activates it through the synchronization process. The following log sample shows a replica database (`slave`) recovering:

```
2024-01-15 10:30:12.345 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterActivationHandler - Starting synchronization of database 'slave' from cluster 'mycluster' using passive synchronization strategy
2024-01-15 10:30:12.567 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterActivationHandler - Completed synchronization of database 'slave' from cluster 'mycluster' using passive synchronization strategy
2024-01-15 10:30:12.568 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterAutoActivationTask - Activated database 'slave' from cluster 'mycluster'
```

When using a `differential` or `full` synchronization strategy, the synchronization logs include row-level details:

```
2024-01-15 10:30:12.345 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterActivationHandler - Starting synchronization of database 'slave' from cluster 'mycluster' using differential synchronization strategy
2024-01-15 10:30:12.400 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sync.DifferentialSynchronizationStrategy - Inserted 15 rows into PUBLIC.ITEMS
2024-01-15 10:30:12.420 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sync.DifferentialSynchronizationStrategy - Updated 3 rows in PUBLIC.ITEMS
2024-01-15 10:30:12.440 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sync.DifferentialSynchronizationStrategy - Deleted 1 rows from PUBLIC.ITEMS
2024-01-15 10:30:12.500 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterActivationHandler - Completed synchronization of database 'slave' from cluster 'mycluster' using differential synchronization strategy
2024-01-15 10:30:12.501 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterAutoActivationTask - Activated database 'slave' from cluster 'mycluster'
```

If activation fails (e.g., the database is still not fully ready), HA-JDBC logs the failure at `WARN` level and retries on the next scheduled auto-activation cycle:

```
2024-01-15 10:29:00.100 WARN  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterOperations - Failed to activate database 'slave' from cluster 'mycluster'. Check database connectivity, credentials, and network configuration
java.sql.SQLException: Connection refused
	at org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(ConnectionFactoryImpl.java:302)
	...
```

#### Typical Failover and Recovery Sequence

A complete sequence showing a database failure followed by recovery:

```
# Normal operation - both databases active
2024-01-15 10:00:00.001 INFO  [main] net.sf.hajdbc.sql.DatabaseClusterImpl - Starting database cluster 'mycluster'
2024-01-15 10:00:00.100 INFO  [main] net.sf.hajdbc.sql.DatabaseClusterImpl - Database cluster 'mycluster' will use initial cluster state [master, slave]

# Slave database goes down
2024-01-15 10:23:45.123 ERROR [hajdbc-failure-detection-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterFailureDetectionTask - Deactivated database 'slave' from cluster 'mycluster'
java.sql.SQLException: Connection refused

# Application continues using master only
2024-01-15 10:23:50.000 INFO  [http-nio-8080-exec-1] org.hibernate.SQL - select * from items

# Slave database recovers and synchronizes
2024-01-15 10:30:12.345 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterActivationHandler - Starting synchronization of database 'slave' from cluster 'mycluster' using passive synchronization strategy
2024-01-15 10:30:12.567 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterActivationHandler - Completed synchronization of database 'slave' from cluster 'mycluster' using passive synchronization strategy
2024-01-15 10:30:12.568 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterAutoActivationTask - Activated database 'slave' from cluster 'mycluster'

# Both databases active again
```

## Using with Flyway

When using HA-JDBC with Flyway for database migrations, configure Flyway to connect to the master database directly, bypassing HA-JDBC.
This ensures migrations are applied only to the primary database, and replicas synchronize automatically.

### Gradle Configuration

Add the Flyway plugin and dependencies alongside HA-JDBC:

```kotlin
plugins {
    id("org.flywaydb.flyway") version "11.1.0"
}

dependencies {
    // HA-JDBC
    implementation("net.sf.hajdbc:ha-jdbc-core:${ha-jdbc.version}")

    // Flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
}

flyway {
    // Connect to the master database directly for migrations
    url = System.getenv("DB_MASTER_URL") ?: "jdbc:postgresql://localhost:5432/sample_db"
    user = System.getenv("DB_USERNAME") ?: "sample_user"
    password = System.getenv("DB_PASSWORD") ?: "sample_password"
}
```

### Spring Boot Configuration

In `application.yml`, configure Flyway to use the master database URL:

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    clean-disabled: true

# Master database URL for Flyway
database:
  master:
    url: ${DB_MASTER_URL:jdbc:postgresql://localhost:5432/sample_db}
    username: ${DB_USERNAME:sample_user}
    password: ${DB_PASSWORD:sample_password}
```

### Key Points

- **Migrate master only**: Always run Flyway migrations against the master database directly, not through HA-JDBC.
- **Disable in tests**: Set `spring.flyway.enabled: false` in test configurations to avoid migration conflicts.
- **Replica synchronization**: Replicas will synchronize automatically via HA-JDBC's replication mechanism after migrations are applied to the master.

## License

[GNU Lesser General Public License](https://www.gnu.org/licenses/lgpl.html)