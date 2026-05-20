package controller;

import client.SocketClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import model.Book;
import model.User;
import view.panels.BooksPanel;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

public class BooksController {

    private final BooksPanel view;
    private final User       user;
    private final Gson       gson = new Gson();
    private Consumer<List<Book>> onBooksLoaded;

    public BooksController(BooksPanel view, User user) {
        this.view = view;
        this.user = user;

        view.setBookSaver((book, isNew, onSuccess, onError) -> {
            new SwingWorker<JsonObject, Void>() {
                @Override protected JsonObject doInBackground() throws Exception {
                    String action = isNew ? "ADD_BOOK" : "UPDATE_BOOK";
                    return SocketClient.getInstance().send(action, user.getToken(), book);
                }
                @Override protected void done() {
                    try {
                        JsonObject res = get();
                        if ("OK".equals(res.get("status").getAsString())) {
                            SwingUtilities.invokeLater(() -> { onSuccess.run(); loadBooks(); });
                        } else {
                            SwingUtilities.invokeLater(() -> onError.accept(res.get("message").getAsString()));
                        }
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
                    }
                }
            }.execute();
        });

        view.setBookDeleter((id, onSuccess, onError) -> {
            new SwingWorker<JsonObject, Void>() {
                @Override protected JsonObject doInBackground() throws Exception {
                    return SocketClient.getInstance().send("DELETE_BOOK", user.getToken(), java.util.Map.of("id", id));
                }
                @Override protected void done() {
                    try {
                        JsonObject res = get();
                        if ("OK".equals(res.get("status").getAsString())) {
                            SwingUtilities.invokeLater(() -> { onSuccess.run(); loadBooks(); });
                        } else {
                            SwingUtilities.invokeLater(() -> onError.accept(res.get("message").getAsString()));
                        }
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
                    }
                }
            }.execute();
        });

        loadBooks();
    }

    public void setOnBooksLoaded(Consumer<List<Book>> callback) { this.onBooksLoaded = callback; }

    private void loadBooks() {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return SocketClient.getInstance().send("GET_BOOKS", user.getToken());
            }
            @Override protected void done() {
                try {
                    JsonObject res = get();
                    if ("OK".equals(res.get("status").getAsString())) {
                        List<Book> books = gson.fromJson(res.get("data"),
                                new TypeToken<List<Book>>(){}.getType());
                        SwingUtilities.invokeLater(() -> {
                            view.setBooks(books);
                            if (onBooksLoaded != null) onBooksLoaded.accept(books);
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }
}
