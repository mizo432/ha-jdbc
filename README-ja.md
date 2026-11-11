# HA-JDBC: High-Availability JDBC

<http://ha-jdbc.github.com>

## ロギング

HA-JDBC は、ServiceLoader メカニズムを通じて複数のロギングフレームワークをサポートするロギング抽象化を提供します。
以下のロギングプロバイダーが利用可能です：

| プロバイダー | アーティファクト | 説明 |
|----------|----------|-------------|
| SLF4J | `:logging:slf4j` | SLF4J ベースのロギング（Spring Boot で推奨） |
| Commons Logging | `:logging:commons` | Apache Commons Logging |
| JDK Logging | Built-in | Java Util Logging（デフォルトのフォールバック） |

### ロギングプロバイダーの追加

コアモジュールと一緒に、希望するロギングプロバイダーを依存関係として追加します：

**Gradle (Kotlin DSL)**
```kotlin
dependencies {
    implementation(project(":core"))
    implementation(project(":logging:slf4j"))  // SLF4J プロバイダー
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

クラスパスに複数のプロバイダーが存在する場合、ServiceLoader によって最初に発見されたものが使用されます。

### ログレベル

HA-JDBC は以下のログレベルをサポートしています：`ERROR`、`WARN`、`INFO`、`DEBUG`、`TRACE`。

### 本番環境向けのロギング設定

本番環境では、可観測性とパフォーマンスのバランスを取るために、以下のログレベルを設定することをお勧めします：

**Spring Boot (`application.yml`)**
```yaml
logging:
  level:
    root: WARN
    net.sf.hajdbc: INFO
    org.hibernate.SQL: WARN
```

| レベル | パッケージ | 推奨事項 |
|-------|---------|----------------|
| `WARN` | `root` | 冗長なフレームワークログを抑制 |
| `INFO` | `net.sf.hajdbc` | HA-JDBC の状態遷移、フェイルオーバーイベント、同期ステータスを記録 |
| `WARN` | `org.hibernate.SQL` | 問題のあるクエリを表示しながら SQL ステートメントのノイズを低減 |

#### ログレベルを調整するタイミング

- **フェイルオーバーの問題のトラブルシューティング**：`net.sf.hajdbc` を `DEBUG` に設定して、詳細なデータベースヘルスチェック結果と接続状態の変更を確認します。
- **パフォーマンス調査**：`net.sf.hajdbc` を `TRACE` に設定して、HA-JDBC を通じてルーティングされるすべての JDBC コールをキャプチャします。
- **監査ログ**：運用上の可視性のために、本番環境では `net.sf.hajdbc` を `INFO` に保ち、フェイルオーバーと同期イベントを記録します。

### ログ出力のサンプル

このセクションでは、一般的な運用シナリオにおけるログ出力のサンプルを提供し、本番環境での HA-JDBC の動作監視を支援します。

#### データベース障害（非アクティブ化）

データベースに接続できなくなった場合、HA-JDBC は障害を検知してクラスタからそのデータベースを非アクティブ化します。以下のログサンプルは、レプリカデータベース（`slave`）が接続拒否によりダウンした様子を示しています：

```
2024-01-15 10:23:45.123 ERROR [hajdbc-failure-detection-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterFailureDetectionTask - Deactivated database 'slave' from cluster 'mycluster'
java.sql.SQLException: Connection refused
	at org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(ConnectionFactoryImpl.java:302)
	at org.postgresql.core.ConnectionFactory.openConnection(ConnectionFactory.java:51)
	at org.postgresql.jdbc.PgConnection.<init>(PgConnection.java:264)
	...
```

アクティブな書き込み操作中にデータベースが障害を起こした場合（すべてのアクティブデータベースを対象とする書き込み操作の `InvokeOnManyInvocationStrategy`）、非アクティブ化をトリガーした例外がログに含まれます：

```
2024-01-15 10:23:45.456 ERROR [http-nio-8080-exec-5] net.sf.hajdbc.invocation.InvokeOnManyInvocationStrategy - Deactivated database 'slave' from cluster 'mycluster'
java.sql.SQLException: Connection refused
	at org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(ConnectionFactoryImpl.java:302)
	...
```

複数のデータベースが障害を起こし、その例外が異なる場合（不一致の障害）、HA-JDBC はその不一致をログに記録します：

```
2024-01-15 10:23:45.789 ERROR [http-nio-8080-exec-5] net.sf.hajdbc.invocation.InvokeOnManyInvocationStrategy - Deactivated database 'slave' from cluster 'mycluster' due to inconsistent operations results: expected [java.sql.SQLException: Connection refused], actual [java.sql.SQLTransientConnectionException: HA100 - Unable to get managed connection]
java.sql.SQLTransientConnectionException: HA100 - Unable to get managed connection
	...
```

クラスタ内のすべてのアクティブデータベースが利用できなくなった場合、HA-JDBC はアプリケーションに例外をスローします：

```
2024-01-15 10:24:00.012 ERROR [http-nio-8080-exec-3] net.sf.hajdbc.invocation.InvokeOnOneInvocationStrategy - Database cluster 'mycluster' has no active databases
```

#### データベース復旧（再アクティブ化）

非アクティブ化されたデータベースが再び利用可能になると、HA-JDBC は自動的に検知して同期プロセスを通じて再アクティブ化します。以下のログサンプルは、レプリカデータベース（`slave`）が復旧した様子を示しています：

```
2024-01-15 10:30:12.345 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterActivationHandler - Starting synchronization of database 'slave' from cluster 'mycluster' using passive synchronization strategy
2024-01-15 10:30:12.567 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterActivationHandler - Completed synchronization of database 'slave' from cluster 'mycluster' using passive synchronization strategy
2024-01-15 10:30:12.568 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterAutoActivationTask - Activated database 'slave' from cluster 'mycluster'
```

`differential` または `full` 同期戦略を使用している場合、同期ログには行レベルの詳細が含まれます：

```
2024-01-15 10:30:12.345 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterActivationHandler - Starting synchronization of database 'slave' from cluster 'mycluster' using differential synchronization strategy
2024-01-15 10:30:12.400 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sync.DifferentialSynchronizationStrategy - Inserted 15 rows into PUBLIC.ITEMS
2024-01-15 10:30:12.420 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sync.DifferentialSynchronizationStrategy - Updated 3 rows in PUBLIC.ITEMS
2024-01-15 10:30:12.440 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sync.DifferentialSynchronizationStrategy - Deleted 1 rows from PUBLIC.ITEMS
2024-01-15 10:30:12.500 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterActivationHandler - Completed synchronization of database 'slave' from cluster 'mycluster' using differential synchronization strategy
2024-01-15 10:30:12.501 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterAutoActivationTask - Activated database 'slave' from cluster 'mycluster'
```

アクティブ化に失敗した場合（データベースがまだ完全に準備できていないなど）、HA-JDBC は `WARN` レベルで失敗をログに記録し、次のスケジュールされた自動アクティブ化サイクルで再試行します：

```
2024-01-15 10:29:00.100 WARN  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterOperations - Failed to activate database 'slave' from cluster 'mycluster'. Check database connectivity, credentials, and network configuration
java.sql.SQLException: Connection refused
	at org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(ConnectionFactoryImpl.java:302)
	...
```

#### 典型的なフェイルオーバーと復旧のシーケンス

データベースの障害から復旧までの完全なシーケンス：

```
# 正常動作 - 両方のデータベースがアクティブ
2024-01-15 10:00:00.001 INFO  [main] net.sf.hajdbc.sql.DatabaseClusterImpl - Starting database cluster 'mycluster'
2024-01-15 10:00:00.100 INFO  [main] net.sf.hajdbc.sql.DatabaseClusterImpl - Database cluster 'mycluster' will use initial cluster state [master, slave]

# Slave データベースがダウン
2024-01-15 10:23:45.123 ERROR [hajdbc-failure-detection-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterFailureDetectionTask - Deactivated database 'slave' from cluster 'mycluster'
java.sql.SQLException: Connection refused

# アプリケーションは master のみを使用して継続
2024-01-15 10:23:50.000 INFO  [http-nio-8080-exec-1] org.hibernate.SQL - select * from items

# Slave データベースが復旧して同期
2024-01-15 10:30:12.345 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterActivationHandler - Starting synchronization of database 'slave' from cluster 'mycluster' using passive synchronization strategy
2024-01-15 10:30:12.567 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterActivationHandler - Completed synchronization of database 'slave' from cluster 'mycluster' using passive synchronization strategy
2024-01-15 10:30:12.568 INFO  [hajdbc-auto-activation-1] net.sf.hajdbc.sql.DatabaseClusterImpl$DatabaseClusterAutoActivationTask - Activated database 'slave' from cluster 'mycluster'

# 両方のデータベースが再びアクティブ
```

## Flyway との併用

HA-JDBC を Flyway と組み合わせてデータベースマイグレーションに使用する場合は、HA-JDBC を経由せずにマスターデータベースに直接 Flyway が接続するように設定します。
これにより、マイグレーションはプライマリデータベースにのみ適用され、レプリカは自動的に同期されます。

### Gradle 設定

HA-JDBC と一緒に Flyway プラグインと依存関係を追加します：

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
    // マイグレーション用にマスターデータベースに直接接続
    url = System.getenv("DB_MASTER_URL") ?: "jdbc:postgresql://localhost:5432/sample_db"
    user = System.getenv("DB_USERNAME") ?: "sample_user"
    password = System.getenv("DB_PASSWORD") ?: "sample_password"
}
```

### Spring Boot 設定

`application.yml` で、Flyway がマスターデータベースの URL を使用するよう設定します：

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    clean-disabled: true

# Flyway 用のマスターデータベース URL
database:
  master:
    url: ${DB_MASTER_URL:jdbc:postgresql://localhost:5432/sample_db}
    username: ${DB_USERNAME:sample_user}
    password: ${DB_PASSWORD:sample_password}
```

### 重要なポイント

- **マスターのみをマイグレート**：Flyway のマイグレーションは HA-JDBC を経由せず、常にマスターデータベースに対して直接実行します。
- **テストでは無効化**：テスト設定で `spring.flyway.enabled: false` を設定し、マイグレーションの競合を避けます。
- **レプリカの同期**：マスターにマイグレーションが適用された後、レプリカは HA-JDBC のレプリケーションメカニズムによって自動的に同期されます。

## ライセンス

[GNU Lesser General Public License](https://www.gnu.org/licenses/lgpl.html)
