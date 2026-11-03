import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // The address where Docker is running our database
    private static final String URL = "jdbc:postgresql://localhost:5432/bank_system";
    private static final String USER = "bank_admin";
    private static final String PASSWORD = "securepassword123";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Connect to the database using our credentials
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Successfully connected to the Bank Database!");
        } catch (SQLException e) {
            System.out.println("Connection failed! Make sure your Docker container is running.");
            e.printStackTrace();
        }
        return connection;
    }

    // A quick main method to test if it works
    public static void main(String[] args) {
        getConnection();
    }
}

