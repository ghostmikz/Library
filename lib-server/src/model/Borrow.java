package model;

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
    public void   setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBorrowerName()                      { return borrowerName; }
    public void   setBorrowerName(String borrowerName)   { this.borrowerName = borrowerName; }

    public String getBorrowerPhone()                     { return borrowerPhone; }
    public void   setBorrowerPhone(String borrowerPhone) { this.borrowerPhone = borrowerPhone; }

    public String getBorrowDate()                    { return borrowDate; }
    public void   setBorrowDate(String borrowDate)   { this.borrowDate = borrowDate; }

    public String getDueDate()                   { return dueDate; }
    public void   setDueDate(String dueDate)     { this.dueDate = dueDate; }

    public String getReturnDate()                    { return returnDate; }
    public void   setReturnDate(String returnDate)   { this.returnDate = returnDate; }

    public String getNotes()                 { return notes; }
    public void   setNotes(String notes)     { this.notes = notes; }
}
