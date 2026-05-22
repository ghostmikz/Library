package controller;

import client.SocketClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import model.Book;
import model.User;
import view.panels.DashboardPanel;

import javax.swing.*;
import java.util.List;

public class DashboardController {

    private final DashboardPanel view;
    private final User           user;
    private final Gson           gson = new Gson();

    public DashboardController(DashboardPanel view, User user) {
        this.view = view;
        this.user = user;
        view.setRefreshListener(this::load);
        load();
    }

    public void reload() { load(); }

    private void load() {
        new SwingWorker<JsonObject, Void>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return SocketClient.getInstance().send("GET_DASHBOARD", user.getToken());
            }
            @Override protected void done() {
                try {
                    JsonObject res = get();
                    if ("OK".equals(res.get("status").getAsString())) {
                        JsonObject data = res.getAsJsonObject("data");
                        int total     = data.get("total").getAsInt();
                        int available = data.get("available").getAsInt();
                        int unavail   = data.get("unavailable").getAsInt();
                        List<Book> recent = gson.fromJson(data.get("recent"),
                                new TypeToken<List<Book>>(){}.getType());
                        view.setStats(total, available, unavail);
                        view.setRecentBooks(recent);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }
}
