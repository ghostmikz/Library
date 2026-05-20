USE lib_db;
DROP PROCEDURE IF EXISTS sp_lib_update_librarian;
DELIMITER $$
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
DELIMITER ;
