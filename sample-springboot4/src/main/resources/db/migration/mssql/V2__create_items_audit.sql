-- Create audit table for Item entity (Hibernate Envers) - MSSQL
CREATE TABLE items_AUD (
    id BIGINT NOT NULL,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500),
    price INT NOT NULL,
    stock INT NOT NULL,
    created_at DATETIME2 NOT NULL,
    updated_at DATETIME2 NOT NULL,
    REV INT NOT NULL,
    REVTYPE SMALLINT,
    PRIMARY KEY (id, REV)
);

-- Create revision information table for Envers
CREATE TABLE revinfo (
    rev INT PRIMARY KEY IDENTITY(1,1),
    revtstmp BIGINT
);
