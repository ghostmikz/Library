USE lib_db;
DROP PROCEDURE IF EXISTS sp_lib_return_book;
DELIMITER $$
CREATE PROCEDURE sp_lib_return_book(IN p_borrow_id INT, IN p_return_date DATE)
BEGIN
    DECLARE v_book_id INT;
    SELECT book_id INTO v_book_id FROM borrows WHERE id = p_borrow_id;
    UPDATE borrows SET return_date = p_return_date WHERE id = p_borrow_id;
    UPDATE books SET available_quantity = available_quantity + 1 WHERE id = v_book_id;
END$$
DELIMITER ;
