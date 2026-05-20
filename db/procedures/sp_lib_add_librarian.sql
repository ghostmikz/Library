USE lib_db;
DROP PROCEDURE IF EXISTS sp_lib_add_librarian;
DELIMITER $$
CREATE PROCEDURE sp_lib_add_librarian(
    IN p_username  VARCHAR(50),
    IN p_password  VARCHAR(255),
    IN p_full_name VARCHAR(100)
)
BEGIN
    INSERT INTO users (username, password_hash, full_name, role)
    VALUES (p_username, p_password, p_full_name, 'librarian');
    SELECT LAST_INSERT_ID() AS user_id;
END$$
DELIMITER ;
