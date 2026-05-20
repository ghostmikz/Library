USE lib_db;
DROP PROCEDURE IF EXISTS sp_lib_set_librarian_active;
DELIMITER $$
CREATE PROCEDURE sp_lib_set_librarian_active(IN p_id INT, IN p_active BOOLEAN)
BEGIN
    UPDATE users SET is_active = p_active WHERE id = p_id AND role = 'librarian';
END$$
DELIMITER ;
