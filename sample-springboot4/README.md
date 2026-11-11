# Spring Boot 4 Sample Project

Spring Boot 4 + JPA + PostgreSQL のサンプルプロジェクトです。

## 技術スタック

- **Java 25**
- **Spring Boot 4.1.2**
- **Spring Data JPA**
- **PostgreSQL 17**
- **Flyway** (データベースマイグレーション)
- **Docker Compose** (ローカル開発環境)

## 起動方法

### アプリケーションを起動

```bash
./gradlew bootRun
```

`bootRun`を実行すると、PostgreSQL コンテナが自動的に起動します。

または IDE から `SampleApplication.java` を実行します（IDE の実行設定で `dockerComposeUp` タスクを依存タスクとして追加してください）。

### コンテナを手動で管理する場合

```bash
# コンテナを起動
docker compose up -d

# コンテナを停止・削除
docker compose down

# ボリュームも削除する場合
docker compose down -v
```

## API エンドポイント

| メソッド | パス | 説明 |
|----------|------|------|
| GET | `/api/items` | 全商品一覧取得 |
| GET | `/api/items/{id}` | 商品詳細取得 |
| GET | `/api/items/search/name?name=xxx` | 名前で検索 |
| GET | `/api/items/search/price?minPrice=1000&maxPrice=5000` | 価格範囲で検索 |
| POST | `/api/items` | 商品登録 |
| PUT | `/api/items/{id}` | 商品更新 |
| DELETE | `/api/items/{id}` | 商品削除 |

### 使用例

```bash
# 全商品取得
curl http://localhost:8080/api/items

# 名前で検索
curl "http://localhost:8080/api/items/search/name?name=Spring"

# 商品登録
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{"name":"テスト商品","description":"説明","price":1000,"stock":10}'
```

## プロジェクト構造

```
sample-springboot4/
├── docker-compose.yml          # PostgreSQL コンテナ設定
├── build.gradle.kts            # Gradle 設定
└── src/
    ├── main/
    │   ├── java/com/example/sample/
    │   │   ├── SampleApplication.java      # メインクラス
    │   │   ├── entity/
    │   │   │   └── Item.java              # JPA エンティティ
    │   │   ├── repository/
    │   │   │   └── ItemRepository.java    # リポジトリ
    │   │   ├── service/
    │   │   │   └── ItemService.java       # ビジネスロジック
    │   │   └── controller/
    │   │       └── ItemController.java    # REST コントローラー
    │   └── resources/
    │       ├── application.yml            # アプリケーション設定
    │       └── db/migration/
    │           └── V1__init_items.sql     # Flyway マイグレーション
    └── test/
        ├── java/com/example/sample/
        │   ├── config/
        │   │   ├── TestPostgreSQLContainer.java    # TestContainer コンテナ定義
        │   │   └── TestcontainerHaJdbcConfig.java  # TestContainer 用 HA-JDBC 設定
        │   └── controller/
        │       └── ItemControllerTestcontainerIntegrationTest.java
        └── resources/
            ├── application-testcontainer.yml       # TestContainer 用設定
            └── test-schema.sql                     # テスト用スキーマ
```

## データベース接続情報

| 項目 | 値 |
|------|-----|
| URL | `jdbc:postgresql://localhost:5432/sample_db` |
| ユーザー | `sample_user` |
| パスワード | `sample_password` |

## TestContainer でのテスト

TestContainer を使用すると、Docker コンテナ上の PostgreSQL で統合テストを実行できます。
ローカルのデータベース環境が不要で、CI 環境でも同じ条件でテストできます。

### 設定ファイル

TestContainer 用の設定は以下のファイルで構成されています。

| ファイル | 説明 |
|----------|------|
| `src/test/java/.../config/TestPostgreSQLContainer.java` | PostgreSQL 17 のマスター/スレーブコンテナ定義 |
| `src/test/java/.../config/TestcontainerHaJdbcConfig.java` | `testcontainer` プロファイル用の HA-JDBC DataSource 設定 |
| `src/test/resources/application-testcontainer.yml` | TestContainer 用のアプリケーション設定 |
| `src/test/resources/test-schema.sql` | テスト用のスキーマ定義 |

### 依存関係

`build.gradle.kts` に Testcontainers の依存関係が追加されています。

```kotlin
testImplementation("org.testcontainers:testcontainers-postgresql")
testImplementation("org.testcontainers:testcontainers-junit-jupiter")
```

### テストの実行

```bash
# TestContainer テストのみ実行
./gradlew :sample-springboot4:test --tests "ItemControllerTestcontainerIntegrationTest"

# 全テスト実行（H2 テストと TestContainer テストの両方）
./gradlew :sample-springboot4:test
```

### テストの仕組み

1. `@ActiveProfiles("testcontainer")` で `testcontainer` プロファイルを有効化
2. `@DynamicPropertySource` で PostgreSQL コンテナを起動し、JDBC URL を動的に設定
3. `TestcontainerHaJdbcConfig` が HA-JDBC の DataSource を PostgreSQL TestContainer 用に構築
4. テスト終了時にコンテナは自動的に停止・削除されます

### プロファイルの違い

| プロファイル | データベース | 用途 |
|-------------|-------------|------|
| (デフォルト) | H2 インメモリ | 高速なローカルテスト |
| `testcontainer` | PostgreSQL 17 (TestContainer) | 本番環境に近い統合テスト |
| `mssql-test` | H2 (MSSQLServer モード) | MSSQL 互換性テスト |

