USE lib_db;
DROP PROCEDURE IF EXISTS sp_lib_login;
DELIMITER $$
CREATE PROCEDURE sp_lib_login(IN p_username VARCHAR(50))
BEGIN
    SELECT id, username, password_hash, full_name, role, is_active FROM users WHERE username = p_username;
END$$
DELIMITER ;
