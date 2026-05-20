package handler;

import com.google.gson.Gson;
import dao.BookDAO;
import model.Book;
import model.Request;
import model.Response;
import model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookHandler {
    private static final Gson    GSON = new Gson();
    private static final BookDAO DAO  = new BookDAO();

    public static Response getAll(Request req, User caller) {
        try {
            return Response.ok(DAO.findAll());
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    public static Response search(Request req, User caller) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> d = GSON.fromJson(req.getData().toString(), Map.class);
            String  query     = (String)  d.get("query");
            String  genre     = (String)  d.get("genre");
            Object  availObj  = d.get("available");
            Boolean available = availObj != null ? (Boolean) availObj : null;
            List<Book> books = DAO.search(query, genre, available);
            return Response.ok(books);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    public static Response add(Request req, User caller) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> d = GSON.fromJson(req.getData().toString(), Map.class);
            String title = (String) d.get("title");
            String author = (String) d.get("author");
            if (title == null || title.isBlank() || author == null || author.isBlank())
                return Response.error("Title and author are required");
            Book b = new Book();
            b.setIsbn((String) d.get("isbn"));
            b.setTitle(title.trim());
            b.setAuthor(author.trim());
            b.setGenre((String) d.get("genre"));
            int qty = d.get("totalQuantity") != null ? ((Number) d.get("totalQuantity")).intValue() : 1;
            b.setTotalQuantity(qty);
            b.setAvailableQuantity(qty);
            int id = DAO.create(b);
            return id > 0 ? Response.ok(id) : Response.error("Failed to add book");
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    public static Response update(Request req, User caller) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> d = GSON.fromJson(req.getData().toString(), Map.class);
            Book b = new Book();
            b.setId(((Number) d.get("id")).intValue());
            b.setIsbn((String) d.get("isbn"));
            b.setTitle((String) d.get("title"));
            b.setAuthor((String) d.get("author"));
            b.setGenre((String) d.get("genre"));
            b.setTotalQuantity(((Number) d.get("totalQuantity")).intValue());
            b.setAvailableQuantity(((Number) d.get("availableQuantity")).intValue());
            DAO.update(b);
            return Response.ok(null);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    public static Response delete(Request req, User caller) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> d = GSON.fromJson(req.getData().toString(), Map.class);
            int id = ((Number) d.get("id")).intValue();
            DAO.delete(id);
            return Response.ok(null);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    public static Response getDashboard(Request req, User caller) {
        try {
            int total     = DAO.countTotal();
            int available = DAO.countAvailable();
            List<Book> recent = DAO.findRecent(5);
            Map<String, Object> data = new HashMap<>();
            data.put("total",       total);
            data.put("available",   available);
            data.put("unavailable", total - available);
            data.put("recent",      recent);
            return Response.ok(data);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }
}
