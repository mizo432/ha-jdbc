-- Create items table
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    price INTEGER NOT NULL,
    stock INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO items (name, description, price, stock) VALUES
    ('Spring Boot 4入門', 'Spring Boot 4の公式ガイドブック', 3500, 100),
    ('PostgreSQL実践ガイド', 'データベース設計から最適化まで', 4200, 50),
    ('Docker完全攻略', 'コンテナ技術のすべて', 2800, 75),
    ('Java 21新機能', '最新Javaの機能を一挙解説', 3000, 60),
    ('マイクロサービス設計', '大規模システムの設計パターン', 4500, 40);
