import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerRepository {

    // 1. Method to insert a new customer into the database (Sign up)
    public boolean saveCustomer(Customer customer) {
        String sql = "INSERT INTO customers (first_name, last_name, email, password_hash) VALUES (?, ?, ?, ?)";
        
        // Open a connection using the class we wrote in Phase 1
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Set the '?' placeholders securely to prevent SQL Injection attacks
            pstmt.setString(1, customer.getFirstName());
            pstmt.setString(2, customer.getLastName());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getPasswordHash());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Returns true if the user was successfully inserted
            
        } catch (SQLException e) {
            System.out.println("❌ Failed to save customer to the database.");
            e.printStackTrace();
            return false;
        }
    }

    // 2. Method to fetch a customer by email (Useful for Login checking)
    public Customer getCustomerByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // If a customer is found, rebuild the Customer object from database rows
                return new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching customer by email.");
            e.printStackTrace();
        }
        return null; // Return null if no customer exists with that email
    }
}
