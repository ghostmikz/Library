USE lib_db;
DROP PROCEDURE IF EXISTS sp_lib_update_book;
DELIMITER $$
CREATE PROCEDURE sp_lib_update_book(
    IN p_id         INT,
    IN p_isbn       VARCHAR(20),
    IN p_title      VARCHAR(200),
    IN p_author     VARCHAR(100),
    IN p_genre      VARCHAR(50),
    IN p_total      INT,
    IN p_available  INT
)
BEGIN
    UPDATE books SET isbn=p_isbn, title=p_title, author=p_author, genre=p_genre,
                     total_quantity=p_total, available_quantity=p_available
    WHERE id = p_id;
END$$
DELIMITER ;
