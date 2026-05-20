package dao;

import model.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public List<Book> findAll() throws SQLException {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT id, isbn, title, author, genre, total_quantity, available_quantity, DATE_FORMAT(created_at,'%Y-%m-%d') AS created_at FROM books ORDER BY title";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Book> search(String query, String genre, Boolean available) throws SQLException {
        List<Book> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT id, isbn, title, author, genre, total_quantity, available_quantity, DATE_FORMAT(created_at,'%Y-%m-%d') AS created_at FROM books WHERE 1=1");
        if (query != null && !query.isBlank())
            sql.append(" AND (title LIKE ? OR author LIKE ? OR isbn LIKE ?)");
        if (genre != null && !genre.isBlank())
            sql.append(" AND genre = ?");
        if (available != null)
            sql.append(available ? " AND available_quantity > 0" : " AND available_quantity = 0");
        sql.append(" ORDER BY title");

        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql.toString())) {
            int idx = 1;
            if (query != null && !query.isBlank()) {
                String like = "%" + query + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (genre != null && !genre.isBlank()) ps.setString(idx++, genre);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public int create(Book b) throws SQLException {
        try (CallableStatement cs = DatabaseConnection.getInstance().prepareCall("{CALL sp_lib_add_book(?,?,?,?,?,?)}")) {
            cs.setString(1, b.getIsbn());
            cs.setString(2, b.getTitle());
            cs.setString(3, b.getAuthor());
            cs.setString(4, b.getGenre());
            cs.setInt(5, b.getTotalQuantity());
            cs.setInt(6, b.getAvailableQuantity());
            ResultSet rs = cs.executeQuery();
            return rs.next() ? rs.getInt("book_id") : -1;
        }
    }

    public void update(Book b) throws SQLException {
        try (CallableStatement cs = DatabaseConnection.getInstance().prepareCall("{CALL sp_lib_update_book(?,?,?,?,?,?,?)}")) {
            cs.setInt(1, b.getId());
            cs.setString(2, b.getIsbn());
            cs.setString(3, b.getTitle());
            cs.setString(4, b.getAuthor());
            cs.setString(5, b.getGenre());
            cs.setInt(6, b.getTotalQuantity());
            cs.setInt(7, b.getAvailableQuantity());
            cs.execute();
        }
    }

    public void delete(int bookId) throws SQLException {
        try (CallableStatement cs = DatabaseConnection.getInstance().prepareCall("{CALL sp_lib_delete_book(?)}")) {
            cs.setInt(1, bookId);
            cs.execute();
        }
    }

    public int countTotal() throws SQLException {
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM books")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countAvailable() throws SQLException {
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM books WHERE available_quantity > 0")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public List<Book> findRecent(int limit) throws SQLException {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT id, isbn, title, author, genre, total_quantity, available_quantity, DATE_FORMAT(created_at,'%Y-%m-%d') AS created_at FROM books ORDER BY created_at DESC LIMIT ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setGenre(rs.getString("genre"));
        b.setTotalQuantity(rs.getInt("total_quantity"));
        b.setAvailableQuantity(rs.getInt("available_quantity"));
        b.setCreatedAt(rs.getString("created_at"));
        return b;
    }
}
