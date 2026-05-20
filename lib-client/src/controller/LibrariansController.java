package controller;

import client.SocketClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import model.User;
import view.panels.LibrariansPanel;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class LibrariansController {

    private final LibrariansPanel view;
    private final User            caller;
    private final Gson            gson = new Gson();

    public LibrariansController(LibrariansPanel view, User caller) {
        this.view   = view;
        this.caller = caller;

        view.setSaver((u, password, isNew, onSuccess, onError) -> {
            new SwingWorker<JsonObject, Void>() {
                @Override protected JsonObject doInBackground() throws Exception {
                    String action = isNew ? "ADD_LIBRARIAN" : "UPDATE_LIBRARIAN";
                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                    if (!isNew) data.put("id", u.getId());
                    data.put("username", u.getUsername());
                    data.put("fullName", u.getFullName());
                    if (password != null && !password.isBlank()) data.put("password", password);
                    return SocketClient.getInstance().send(action, caller.getToken(), data);
                }
                @Override protected void done() {
                    try {
                        JsonObject res = get();
                        if ("OK".equals(res.get("status").getAsString())) {
                            SwingUtilities.invokeLater(() -> { onSuccess.run(); load(); });
                        } else {
                            SwingUtilities.invokeLater(() -> onError.accept(res.get("message").getAsString()));
                        }
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
                    }
                }
            }.execute();
        });

        view.setToggler((id, active, onSuccess, onError) -> {
            new SwingWorker<JsonObject, Void>() {
                @Override protected JsonObject doInBackground() throws Exception {
                    return SocketClient.getInstance().send("SET_LIBRARIAN_ACTIVE", caller.getToken(),
                            Map.of("id", id, "active", active));
                }
                @Override protected void done() {
                    try {
                        JsonObject res = get();
                        if ("OK".equals(res.get("status").getAsString())) {
                            SwingUtilities.invokeLater(() -> { onSuccess.run(); load(); });
                        } else {
                            SwingUtilities.invokeLater(() -> onError.accept(res.get("message").getAsString()));
                        }
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
                    }
                }
            }.execute();
        });

        view.setDeleter((id, onSuccess, onError) -> {
            new SwingWorker<JsonObject, Void>() {
                @Override protected JsonObject doInBackground() throws Exception {
                    return SocketClient.getInstance().send("DELETE_LIBRARIAN", caller.getToken(),
                            Map.of("id", id));
                }
                @Override protected void done() {
                    try {
                        JsonObject res = get();
                        if ("OK".equals(res.get("status").getAsString())) {
                            SwingUtilities.invokeLater(() -> { onSuccess.run(); load(); });
                        } else {
                            SwingUtilities.invokeLater(() -> onError.accept(res.get("message").getAsString()));
                        }
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
                    }
                }
            }.execute();
        });

        load();
    }

    private void load() {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return SocketClient.getInstance().send("GET_LIBRARIANS", caller.getToken());
            }
            @Override protected void done() {
                try {
                    JsonObject res = get();
                    if ("OK".equals(res.get("status").getAsString())) {
                        List<User> users = gson.fromJson(res.get("data"),
                                new TypeToken<List<User>>(){}.getType());
                        SwingUtilities.invokeLater(() -> view.setLibrarians(users));
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }
}
