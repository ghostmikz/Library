USE lib_db;
DROP PROCEDURE IF EXISTS sp_lib_delete_librarian;
DELIMITER $$
CREATE PROCEDURE sp_lib_delete_librarian(IN p_id INT)
BEGIN
    DELETE FROM users WHERE id = p_id AND role = 'librarian';
END$$
DELIMITER ;
