USE lib_db;
DROP PROCEDURE IF EXISTS sp_lib_delete_book;
DELIMITER $$
CREATE PROCEDURE sp_lib_delete_book(IN p_id INT)
BEGIN
    DELETE FROM books WHERE id = p_id;
END$$
DELIMITER ;
