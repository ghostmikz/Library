package handler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dao.BorrowDAO;
import model.Borrow;
import model.Request;
import model.Response;
import model.User;

import java.util.List;
import java.util.Map;

public class BorrowHandler {

    private static final Gson gson = new Gson();

    public static Response getAll(Request req, User caller) {
        try {
            boolean activeOnly = false;
            if (req.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> d = (Map<String, Object>) req.getData();
                Object ao = d.get("activeOnly");
                if (ao instanceof Boolean) activeOnly = (Boolean) ao;
            }
            List<Borrow> borrows = new BorrowDAO().getAll(activeOnly);
            JsonArray arr = gson.toJsonTree(borrows).getAsJsonArray();
            JsonObject data = new JsonObject();
            data.add("borrows", arr);
            return Response.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error(e.getMessage());
        }
    }

    public static Response add(Request req, User caller) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> d = (Map<String, Object>) req.getData();
            Borrow borrow = new Borrow();
            borrow.setBookId(((Number) d.get("bookId")).intValue());
            borrow.setBorrowerName((String) d.get("borrowerName"));
            borrow.setBorrowerPhone((String) d.get("borrowerPhone"));
            borrow.setBorrowDate((String) d.get("borrowDate"));
            borrow.setDueDate((String) d.get("dueDate"));
            borrow.setNotes((String) d.get("notes"));
            int id = new BorrowDAO().create(borrow);
            JsonObject data = new JsonObject();
            data.addProperty("id", id);
            return Response.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error(e.getMessage());
        }
    }

    public static Response returnBook(Request req, User caller) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> d = (Map<String, Object>) req.getData();
            int borrowId = ((Number) d.get("borrowId")).intValue();
            String returnDate = (String) d.get("returnDate");
            new BorrowDAO().returnBook(borrowId, returnDate);
            return Response.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error(e.getMessage());
        }
    }
}
