-- Drop tables if they exist (Useful for rebuilding/testing your database)
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS customers CASCADE;

-- 1. Customers Table
CREATE TABLE customers (
    customer_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Accounts Table
CREATE TABLE accounts (
    account_number SERIAL PRIMARY KEY,
    customer_id INT NOT NULL,
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('CHECKING', 'SAVINGS')),
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- 3. Transactions Table
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    source_account_number INT,
    destination_account_number INT,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_source_account FOREIGN KEY (source_account_number) REFERENCES accounts(account_number) ON DELETE SET NULL,
    CONSTRAINT fk_dest_account FOREIGN KEY (destination_account_number) REFERENCES accounts(account_number) ON DELETE SET NULL
);
