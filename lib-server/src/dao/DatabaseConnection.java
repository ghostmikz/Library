package dao;

import server.ServerSettings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DatabaseConnection {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    private static final int POOL_SIZE = 5;
    private static final BlockingQueue<Connection> pool = new ArrayBlockingQueue<>(POOL_SIZE);
    private static final ThreadLocal<Connection> THREAD_CONN = new ThreadLocal<>();

    static {
        String host = ServerSettings.getDbHost();
        int    port = ServerSettings.getDbPort();
        URL      = "jdbc:mysql://" + host + ":" + port +
                   "/lib_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true" +
                   "&characterEncoding=UTF-8&useUnicode=true" +
                   "&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048";
        USER     = ServerSettings.getDbUser();
        PASSWORD = ServerSettings.getDbPassword();

        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                pool.offer(DriverManager.getConnection(URL, USER, PASSWORD));
            } catch (SQLException e) {
                System.err.println("Pool pre-warm failed (" + i + "): " + e.getMessage());
            }
        }
    }

    private DatabaseConnection() {}

    public static Connection getInstance() throws SQLException {
        Connection c = THREAD_CONN.get();
        if (!isValid(c)) {
            c = pool.poll();
            if (!isValid(c)) {
                c = DriverManager.getConnection(URL, USER, PASSWORD);
            }
            THREAD_CONN.set(c);
        }
        return c;
    }

    public static void close() {
        Connection c = THREAD_CONN.get();
        THREAD_CONN.remove();
        if (c == null) return;
        try {
            if (!c.isClosed()) {
                if (pool.size() < POOL_SIZE) {
                    pool.offer(c);
                    return;
                }
                c.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean isValid(Connection c) {
        try {
            return c != null && !c.isClosed() && c.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }
}
