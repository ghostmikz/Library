package model;

import java.time.LocalDate;

public class Borrow {
    private int id;
    private int bookId;
    private String bookTitle;
    private String borrowerName;
    private String borrowerPhone;
    private String borrowDate;
    private String dueDate;
    private String returnDate;
    private String notes;

    public Borrow() {}

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id = id; }

    public int    getBookId()                    { return bookId; }
    public void   setBookId(int bookId)          { this.bookId = bookId; }

    public String getBookTitle()                 { return bookTitle; }
    public void   setBookTitle(String v)         { this.bookTitle = v; }

    public String getBorrowerName()              { return borrowerName; }
    public void   setBorrowerName(String v)      { this.borrowerName = v; }

    public String getBorrowerPhone()             { return borrowerPhone; }
    public void   setBorrowerPhone(String v)     { this.borrowerPhone = v; }

    public String getBorrowDate()                { return borrowDate; }
    public void   setBorrowDate(String v)        { this.borrowDate = v; }

    public String getDueDate()                   { return dueDate; }
    public void   setDueDate(String v)           { this.dueDate = v; }

    public String getReturnDate()                { return returnDate; }
    public void   setReturnDate(String v)        { this.returnDate = v; }

    public String getNotes()                     { return notes; }
    public void   setNotes(String v)             { this.notes = v; }

    public boolean isReturned() {
        return returnDate != null && !returnDate.isBlank();
    }

    public boolean isOverdue() {
        if (isReturned() || dueDate == null) return false;
        try { return LocalDate.now().isAfter(LocalDate.parse(dueDate)); }
        catch (Exception e) { return false; }
    }
}
