USE lib_db;
DROP PROCEDURE IF EXISTS sp_lib_add_book;
DELIMITER $$
CREATE PROCEDURE sp_lib_add_book(
    IN p_isbn       VARCHAR(20),
    IN p_title      VARCHAR(200),
    IN p_author     VARCHAR(100),
    IN p_genre      VARCHAR(50),
    IN p_total      INT,
    IN p_available  INT
)
BEGIN
    INSERT INTO books (isbn, title, author, genre, total_quantity, available_quantity)
    VALUES (p_isbn, p_title, p_author, p_genre, p_total, p_available);
    SELECT LAST_INSERT_ID() AS book_id;
END$$
DELIMITER ;
