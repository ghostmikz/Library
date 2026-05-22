package controller;

import client.SocketClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.Book;
import model.Borrow;
import view.panels.BorrowsPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowsController {

    private final BorrowsPanel view;
    private final Gson gson = new Gson();
    private String token;
    private Runnable mutationListener;

    public BorrowsController(BorrowsPanel view, String token) {
        this.view  = view;
        this.token = token;

        view.setLender(this::doLend);
        view.setReturner(this::doReturn);
        view.setRefreshListener(this::load);
        load();
    }

    public void updateBooks(List<Book> books)      { view.setAvailableBooks(books); }
    public void setMutationListener(Runnable r)    { this.mutationListener = r; }

    private void load() {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                JsonObject params = new JsonObject();
                params.addProperty("activeOnly", view.isActiveOnly());
                return SocketClient.getInstance().send("GET_BORROWS", token, params);
            }
            @Override protected void done() {
                try {
                    JsonObject res = get();
                    if ("OK".equals(res.get("status").getAsString())) {
                        JsonArray arr = res.getAsJsonObject("data").getAsJsonArray("borrows");
                        List<Borrow> list = new ArrayList<>();
                        for (var el : arr) list.add(gson.fromJson(el, Borrow.class));
                        view.setBorrows(list);
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void doLend(int bookId, String borrowerName, String phone, String borrowDate,
                        String dueDate, String notes, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                JsonObject data = new JsonObject();
                data.addProperty("bookId",       bookId);
                data.addProperty("borrowerName", borrowerName);
                data.addProperty("borrowerPhone", phone.isBlank() ? null : phone);
                data.addProperty("borrowDate",   borrowDate);
                data.addProperty("dueDate",      dueDate);
                data.addProperty("notes",        notes.isBlank() ? null : notes);
                return SocketClient.getInstance().send("ADD_BORROW", token, data);
            }
            @Override protected void done() {
                try {
                    JsonObject res = get();
                    if ("OK".equals(res.get("status").getAsString())) {
                        SwingUtilities.invokeLater(onSuccess);
                        load();
                        if (mutationListener != null) SwingUtilities.invokeLater(mutationListener);
                    } else {
                        onError.accept(res.get("message").getAsString());
                    }
                } catch (Exception ex) { onError.accept(ex.getMessage()); }
            }
        }.execute();
    }

    private void doReturn(int borrowId, String returnDate, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                JsonObject data = new JsonObject();
                data.addProperty("borrowId",   borrowId);
                data.addProperty("returnDate", returnDate);
                return SocketClient.getInstance().send("RETURN_BORROW", token, data);
            }
            @Override protected void done() {
                try {
                    JsonObject res = get();
                    if ("OK".equals(res.get("status").getAsString())) {
                        SwingUtilities.invokeLater(onSuccess);
                        load();
                        if (mutationListener != null) SwingUtilities.invokeLater(mutationListener);
                    } else {
                        onError.accept(res.get("message").getAsString());
                    }
                } catch (Exception ex) { onError.accept(ex.getMessage()); }
            }
        }.execute();
    }
}
