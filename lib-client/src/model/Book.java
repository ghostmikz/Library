package model;

public class Book {
    private int id;
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private int totalQuantity;
    private int availableQuantity;
    private String createdAt;

    public Book() {}

    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public String getIsbn()                   { return isbn; }
    public void setIsbn(String isbn)          { this.isbn = isbn; }

    public String getTitle()                  { return title; }
    public void setTitle(String title)        { this.title = title; }

    public String getAuthor()                 { return author; }
    public void setAuthor(String author)      { this.author = author; }

    public String getGenre()                  { return genre; }
    public void setGenre(String genre)        { this.genre = genre; }

    public int getTotalQuantity()                         { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity)       { this.totalQuantity = totalQuantity; }

    public int getAvailableQuantity()                           { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity)     { this.availableQuantity = availableQuantity; }

    public String getCreatedAt()                  { return createdAt; }
    public void setCreatedAt(String createdAt)    { this.createdAt = createdAt; }

    public boolean isAvailable() { return availableQuantity > 0; }
}
