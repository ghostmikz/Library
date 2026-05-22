package server;

import java.io.*;
import java.util.Properties;

public class ServerSettings {
    private static final Properties props = new Properties();

    static {
        File f = new File("server.properties");
        if (f.exists()) {
            try (FileInputStream in = new FileInputStream(f)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Warning: could not read server.properties — using defaults.");
            }
        } else {
            System.out.println("server.properties not found — using built-in defaults.");
        }
    }

    private ServerSettings() {}

    public static int    getServerPort()  {
        try { return Integer.parseInt(props.getProperty("server.port", "9091")); }
        catch (NumberFormatException e) { return 9091; }
    }

    public static String getDbHost()     { return props.getProperty("db.host",     "localhost"); }
    public static int    getDbPort()     {
        try { return Integer.parseInt(props.getProperty("db.port", "3306")); }
        catch (NumberFormatException e) { return 3306; }
    }
    public static String getDbUser()     { return props.getProperty("db.user",     "root"); }
    public static String getDbPassword() { return props.getProperty("db.password", "0312"); }
}
