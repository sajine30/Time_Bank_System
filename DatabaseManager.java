import java.sql.*;
import javax.swing.JOptionPane;

/**
 * Utility class for handling database connections and operations.
 * NOTE: Replace the database credentials with your own.
 */
public class DatabaseManager {

    // Database credentials (MUST BE UPDATED)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/TimeBankDB";
    private static final String USER = "root"; // Your MySQL username
    private static final String PASS = "Pranav0206"; // <--- CHANGE THIS!

    // JDBC Driver name
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: MySQL JDBC Driver not found.", "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Establishes a connection to the database.
     * @return Connection object, or null if connection fails.
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            System.err.println("Database Connection Failed!");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database connection failed. Check console for details.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Utility method to close resources quietly.
     */
    public static void close(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                // Log and ignore closing errors
                e.printStackTrace();
            }
        }
    }
}
