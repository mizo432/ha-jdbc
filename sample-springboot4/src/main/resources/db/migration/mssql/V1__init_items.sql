-- Create items table (MSSQL)
CREATE TABLE items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500),
    price INT NOT NULL,
    stock INT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- Insert sample data
INSERT INTO items (name, description, price, stock) VALUES
    (N'Spring Boot 4入門', N'Spring Boot 4の公式ガイドブック', 3500, 100),
    (N'PostgreSQL実践ガイド', N'データベース設計から最適化まで', 4200, 50),
    (N'Docker完全攻略', N'コンテナ技術のすべて', 2800, 75),
    (N'Java 21新機能', N'最新Javaの機能を一挙解説', 3000, 60),
    (N'マイクロサービス設計', N'大規模システムの設計パターン', 4500, 40);
