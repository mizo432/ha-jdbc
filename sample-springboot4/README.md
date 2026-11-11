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
        └── java/com/example/sample/
```

## データベース接続情報

| 項目 | 値 |
|------|-----|
| URL | `jdbc:postgresql://localhost:5432/sample_db` |
| ユーザー | `sample_user` |
| パスワード | `sample_password` |

