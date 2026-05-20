-- V1__init_account_table

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE account_roles (
    account_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_account_roles_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
);

-- Indice para busquedas
CREATE INDEX idx_account_roles_account_id ON account_roles(account_id);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    account_id UUID NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    replaced_by_id UUID,
    device_id VARCHAR(255),
    
    CONSTRAINT fk_refresh_tokens_account 
        FOREIGN KEY (account_id) 
        REFERENCES accounts(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_refresh_tokens_replaced_by 
        FOREIGN KEY (replaced_by_id) 
        REFERENCES refresh_tokens(id) 
        ON DELETE SET NULL
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

CREATE INDEX idx_refresh_tokens_account_id ON refresh_tokens(account_id);

CREATE INDEX idx_refresh_tokens_account_device ON refresh_tokens(account_id, device_id);