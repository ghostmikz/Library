package dao;

import server.ServerSettings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        String host = ServerSettings.getDbHost();
        int    port = ServerSettings.getDbPort();
        URL      = "jdbc:mysql://" + host + ":" + port + "/lib_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useUnicode=true";
        USER     = ServerSettings.getDbUser();
        PASSWORD = ServerSettings.getDbPassword();
    }

    private static final ThreadLocal<Connection> THREAD_CONN = new ThreadLocal<>();

    private DatabaseConnection() {}

    public static Connection getInstance() throws SQLException {
        Connection c = THREAD_CONN.get();
        if (c == null || c.isClosed()) {
            c = DriverManager.getConnection(URL, USER, PASSWORD);
            THREAD_CONN.set(c);
        }
        return c;
    }

    public static void close() {
        Connection c = THREAD_CONN.get();
        try {
            if (c != null && !c.isClosed()) c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            THREAD_CONN.remove();
        }
    }
}
