package server;

import dao.DatabaseConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibraryServer {
    public static void main(String[] args) {
        int PORT = ServerSettings.getServerPort();
        System.out.println("Library Server starting on port " + PORT + "...");
        try {
            DatabaseConnection.getInstance();
            System.out.println("Database connected successfully.");
        } catch (Exception e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            System.exit(1);
        }

        ExecutorService pool = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server ready. Waiting for clients...");
            while (true) {
                Socket client = serverSocket.accept();
                client.setTcpNoDelay(true);
                System.out.println("Client connected: " + client.getInetAddress());
                pool.execute(new ClientHandler(client));
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            DatabaseConnection.close();
        }
    }
}
