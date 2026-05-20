package dao;

import model.Borrow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {

    public List<Borrow> getAll(boolean activeOnly) throws SQLException {
        String sql = "SELECT b.id, b.book_id, bk.title AS book_title, b.borrower_name, b.borrower_phone, " +
                     "DATE_FORMAT(b.borrow_date,'%Y-%m-%d') AS borrow_date, " +
                     "DATE_FORMAT(b.due_date,'%Y-%m-%d') AS due_date, " +
                     "DATE_FORMAT(b.return_date,'%Y-%m-%d') AS return_date, b.notes " +
                     "FROM borrows b JOIN books bk ON b.book_id = bk.id " +
                     (activeOnly ? "WHERE b.return_date IS NULL " : "") +
                     "ORDER BY b.due_date ASC";
        List<Borrow> list = new ArrayList<>();
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public int create(Borrow borrow) throws SQLException {
        try (CallableStatement cs = DatabaseConnection.getInstance()
                .prepareCall("{CALL sp_lib_add_borrow(?,?,?,?,?,?)}")) {
            cs.setInt(1, borrow.getBookId());
            cs.setString(2, borrow.getBorrowerName());
            cs.setString(3, borrow.getBorrowerPhone());
            cs.setString(4, borrow.getBorrowDate());
            cs.setString(5, borrow.getDueDate());
            cs.setString(6, borrow.getNotes());
            ResultSet rs = cs.executeQuery();
            return rs.next() ? rs.getInt("borrow_id") : -1;
        }
    }

    public void returnBook(int borrowId, String returnDate) throws SQLException {
        try (CallableStatement cs = DatabaseConnection.getInstance()
                .prepareCall("{CALL sp_lib_return_book(?,?)}")) {
            cs.setInt(1, borrowId);
            cs.setString(2, returnDate);
            cs.execute();
        }
    }

    private Borrow mapRow(ResultSet rs) throws SQLException {
        Borrow b = new Borrow();
        b.setId(rs.getInt("id"));
        b.setBookId(rs.getInt("book_id"));
        b.setBookTitle(rs.getString("book_title"));
        b.setBorrowerName(rs.getString("borrower_name"));
        b.setBorrowerPhone(rs.getString("borrower_phone"));
        b.setBorrowDate(rs.getString("borrow_date"));
        b.setDueDate(rs.getString("due_date"));
        b.setReturnDate(rs.getString("return_date"));
        b.setNotes(rs.getString("notes"));
        return b;
    }
}
