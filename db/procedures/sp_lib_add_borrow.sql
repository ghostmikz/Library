USE lib_db;
DROP PROCEDURE IF EXISTS sp_lib_add_borrow;
DELIMITER $$
CREATE PROCEDURE sp_lib_add_borrow(
    IN p_book_id        INT,
    IN p_borrower_name  VARCHAR(100),
    IN p_borrower_phone VARCHAR(30),
    IN p_borrow_date    DATE,
    IN p_due_date       DATE,
    IN p_notes          TEXT
)
BEGIN
    INSERT INTO borrows (book_id, borrower_name, borrower_phone, borrow_date, due_date, notes)
    VALUES (p_book_id, p_borrower_name, p_borrower_phone, p_borrow_date, p_due_date, p_notes);
    UPDATE books SET available_quantity = GREATEST(available_quantity - 1, 0) WHERE id = p_book_id;
    SELECT LAST_INSERT_ID() AS borrow_id;
END$$
DELIMITER ;
