-- Create items table
CREATE TABLE IF NOT EXISTS items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    price INTEGER NOT NULL,
    stock INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create audit table for Item entity (Hibernate Envers)
CREATE TABLE IF NOT EXISTS items_AUD (
    id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    price INTEGER NOT NULL,
    stock INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    REV INTEGER NOT NULL,
    REVTYPE SMALLINT,
    PRIMARY KEY (id, REV)
);

-- Create revision information table for Envers
CREATE TABLE IF NOT EXISTS revinfo (
    rev INTEGER PRIMARY KEY,
    revtstmp BIGINT
);
