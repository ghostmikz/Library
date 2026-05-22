-- ============================================================
-- NMIT Library — full database setup (tables + procedures)
-- Run once as root:  mysql -u root -p < db/schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS lib_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lib_db;

-- Application user (connects from any host — required for LAN).
-- Change the password here AND in server.properties if desired.
CREATE USER IF NOT EXISTS 'lib_user'@'%' IDENTIFIED BY 'lib1234';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON lib_db.* TO 'lib_user'@'%';
FLUSH PRIVILEGES;

-- ── Tables ──────────────────────────────────────────────────

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

-- ── Stored Procedures ────────────────────────────────────────

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_lib_login$$
CREATE PROCEDURE sp_lib_login(IN p_username VARCHAR(50))
BEGIN
    SELECT id, username, password_hash, full_name, role, is_active
    FROM users WHERE username = p_username;
END$$

DROP PROCEDURE IF EXISTS sp_lib_add_librarian$$
CREATE PROCEDURE sp_lib_add_librarian(
    IN p_username   VARCHAR(50),
    IN p_password   VARCHAR(255),
    IN p_full_name  VARCHAR(100),
    IN p_created_by INT
)
BEGIN
    INSERT INTO users (username, password_hash, full_name, role)
    VALUES (p_username, p_password, p_full_name, 'librarian');
    SELECT LAST_INSERT_ID() AS user_id;
END$$

DROP PROCEDURE IF EXISTS sp_lib_update_librarian$$
CREATE PROCEDURE sp_lib_update_librarian(
    IN p_id        INT,
    IN p_full_name VARCHAR(100),
    IN p_password  VARCHAR(255)
)
BEGIN
    IF p_password IS NOT NULL AND p_password != '' THEN
        UPDATE users SET full_name=p_full_name, password_hash=p_password WHERE id=p_id;
    ELSE
        UPDATE users SET full_name=p_full_name WHERE id=p_id;
    END IF;
END$$

DROP PROCEDURE IF EXISTS sp_lib_delete_librarian$$
CREATE PROCEDURE sp_lib_delete_librarian(IN p_id INT)
BEGIN
    DELETE FROM users WHERE id = p_id AND role = 'librarian';
END$$

DROP PROCEDURE IF EXISTS sp_lib_set_librarian_active$$
CREATE PROCEDURE sp_lib_set_librarian_active(IN p_id INT, IN p_active BOOLEAN)
BEGIN
    UPDATE users SET is_active = p_active WHERE id = p_id AND role = 'librarian';
END$$

DROP PROCEDURE IF EXISTS sp_lib_add_book$$
CREATE PROCEDURE sp_lib_add_book(
    IN p_isbn      VARCHAR(20),
    IN p_title     VARCHAR(200),
    IN p_author    VARCHAR(100),
    IN p_genre     VARCHAR(50),
    IN p_total     INT,
    IN p_available INT
)
BEGIN
    IF p_available > p_total THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Available quantity cannot exceed total quantity';
    END IF;
    IF p_isbn = '' THEN SET p_isbn = NULL; END IF;
    INSERT INTO books (isbn, title, author, genre, total_quantity, available_quantity)
    VALUES (p_isbn, p_title, p_author, p_genre, p_total, p_available);
    SELECT LAST_INSERT_ID() AS book_id;
END$$

DROP PROCEDURE IF EXISTS sp_lib_update_book$$
CREATE PROCEDURE sp_lib_update_book(
    IN p_id        INT,
    IN p_isbn      VARCHAR(20),
    IN p_title     VARCHAR(200),
    IN p_author    VARCHAR(100),
    IN p_genre     VARCHAR(50),
    IN p_total     INT,
    IN p_available INT
)
BEGIN
    IF p_available > p_total THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Available quantity cannot exceed total quantity';
    END IF;
    IF p_isbn = '' THEN SET p_isbn = NULL; END IF;
    UPDATE books
    SET isbn=p_isbn, title=p_title, author=p_author, genre=p_genre,
        total_quantity=p_total, available_quantity=p_available
    WHERE id = p_id;
END$$

DROP PROCEDURE IF EXISTS sp_lib_delete_book$$
CREATE PROCEDURE sp_lib_delete_book(IN p_id INT)
BEGIN
    DECLARE v_active INT DEFAULT 0;
    SELECT COUNT(*) INTO v_active FROM borrows WHERE book_id = p_id AND return_date IS NULL;
    IF v_active > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot delete book with active borrows';
    END IF;
    DELETE FROM books WHERE id = p_id;
END$$

DROP PROCEDURE IF EXISTS sp_lib_add_borrow$$
CREATE PROCEDURE sp_lib_add_borrow(
    IN p_book_id        INT,
    IN p_borrower_name  VARCHAR(100),
    IN p_borrower_phone VARCHAR(30),
    IN p_borrow_date    DATE,
    IN p_due_date       DATE,
    IN p_notes          TEXT
)
BEGIN
    DECLARE v_avail INT DEFAULT 0;
    SELECT available_quantity INTO v_avail FROM books WHERE id = p_book_id FOR UPDATE;
    IF v_avail <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No available copies';
    END IF;
    INSERT INTO borrows (book_id, borrower_name, borrower_phone, borrow_date, due_date, notes)
    VALUES (p_book_id, p_borrower_name, p_borrower_phone, p_borrow_date, p_due_date, p_notes);
    UPDATE books SET available_quantity = available_quantity - 1 WHERE id = p_book_id;
    SELECT LAST_INSERT_ID() AS borrow_id;
END$$

DROP PROCEDURE IF EXISTS sp_lib_return_book$$
CREATE PROCEDURE sp_lib_return_book(IN p_borrow_id INT, IN p_return_date DATE)
BEGIN
    DECLARE v_book_id   INT;
    DECLARE v_returned  DATE;
    SELECT book_id, return_date INTO v_book_id, v_returned
    FROM borrows WHERE id = p_borrow_id;
    IF v_returned IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Book already returned';
    END IF;
    UPDATE borrows SET return_date = p_return_date WHERE id = p_borrow_id;
    UPDATE books SET available_quantity = available_quantity + 1 WHERE id = v_book_id;
END$$

DELIMITER ;
