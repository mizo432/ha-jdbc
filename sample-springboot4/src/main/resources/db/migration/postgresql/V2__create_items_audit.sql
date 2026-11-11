-- Create audit table for Item entity (Hibernate Envers)
-- Default audit table name: items_AUD (table name + _AUD suffix)
CREATE TABLE items_AUD (
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
CREATE TABLE revinfo (
    rev INTEGER PRIMARY KEY,
    revtstmp BIGINT
);
