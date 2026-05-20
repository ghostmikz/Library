package server;

import com.google.gson.Gson;
import handler.BookHandler;
import handler.BorrowHandler;
import handler.LoginHandler;
import handler.UserHandler;
import model.Request;
import model.Response;
import model.User;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Gson gson = new Gson();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream(),  "UTF-8"));
             PrintWriter   out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)) {
            String line;
            while ((line = in.readLine()) != null) {
                out.println(gson.toJson(handle(line)));
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            dao.DatabaseConnection.close();
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private Response handle(String json) {
        try {
            Request req = gson.fromJson(json, Request.class);
            String action = req.getAction();

            if ("LOGIN".equals(action)) return LoginHandler.handle(req);

            User user = SessionManager.getUser(req.getToken());
            if (user == null) return Response.error("Unauthorized — please log in");

            return switch (action) {
                case "LOGOUT"             -> LoginHandler.logout(req);
                case "GET_BOOKS"          -> BookHandler.getAll(req, user);
                case "SEARCH_BOOKS"       -> BookHandler.search(req, user);
                case "ADD_BOOK"           -> BookHandler.add(req, user);
                case "UPDATE_BOOK"        -> BookHandler.update(req, user);
                case "DELETE_BOOK"        -> BookHandler.delete(req, user);
                case "GET_DASHBOARD"      -> BookHandler.getDashboard(req, user);
                case "GET_LIBRARIANS"     -> UserHandler.getAll(req, user);
                case "ADD_LIBRARIAN"      -> UserHandler.add(req, user);
                case "UPDATE_LIBRARIAN"   -> UserHandler.update(req, user);
                case "DELETE_LIBRARIAN"   -> UserHandler.delete(req, user);
                case "SET_LIBRARIAN_ACTIVE" -> UserHandler.setActive(req, user);
                case "GET_BORROWS"        -> BorrowHandler.getAll(req, user);
                case "ADD_BORROW"         -> BorrowHandler.add(req, user);
                case "RETURN_BORROW"      -> BorrowHandler.returnBook(req, user);
                default -> Response.error("Unknown action: " + action);
            };
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("Server error: " + e.getMessage());
        }
    }
}
