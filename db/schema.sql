CREATE DATABASE IF NOT EXISTS lib_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lib_db;

-- Application user: can connect from any host (needed for LAN deployments).
-- Run this as root. Change the password here AND in server.properties if desired.
CREATE USER IF NOT EXISTS 'lib_user'@'%' IDENTIFIED BY 'lib1234';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON lib_db.* TO 'lib_user'@'%';
FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS users (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(50)  UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    role          ENUM('admin','librarian') NOT NULL,
    is_active     BOOLEAN DEFAULT TRUE
);

INSERT IGNORE INTO users (username, password_hash, full_name, role)
VALUES ('admin', '123', 'Administrator', 'admin');

CREATE TABLE IF NOT EXISTS books (
    id                 INT PRIMARY KEY AUTO_INCREMENT,
    isbn               VARCHAR(20) UNIQUE,
    title              VARCHAR(200) NOT NULL,
    author             VARCHAR(100) NOT NULL,
    genre              VARCHAR(50),
    total_quantity     INT DEFAULT 1,
    available_quantity INT DEFAULT 1,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS borrows (
    id             INT PRIMARY KEY AUTO_INCREMENT,
    book_id        INT NOT NULL,
    borrower_name  VARCHAR(100) NOT NULL,
    borrower_phone VARCHAR(30),
    borrow_date    DATE NOT NULL,
    due_date       DATE NOT NULL,
    return_date    DATE,
    notes          TEXT,
    FOREIGN KEY (book_id) REFERENCES books(id)
);
