package repository;

import config.DatabaseConnection;
import model.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerRepository {

    public CustomerRepository() {
        ensureSchema();
    }

    // Auto-migrate database table columns if they do not exist
    private void ensureSchema() {
        String[] alterQueries = new String[] {
            "ALTER TABLE customers ADD COLUMN IF NOT EXISTS phone VARCHAR(30)",
            "ALTER TABLE customers ADD COLUMN IF NOT EXISTS address VARCHAR(255)",
            "ALTER TABLE customers ADD COLUMN IF NOT EXISTS pan_number VARCHAR(30)",
            "ALTER TABLE customers ADD COLUMN IF NOT EXISTS date_of_birth VARCHAR(30)"
        };

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String q : alterQueries) {
                stmt.execute(q);
            }
        } catch (SQLException e) {
            // Table may already be updated or connection will report on first operation
        }
    }

    // 1. Method to insert a new customer into the database (Sign up)
    public boolean saveCustomer(Customer customer) {
        String sql = "INSERT INTO customers (first_name, last_name, email, password_hash, phone, address, pan_number, date_of_birth) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customer.getFirstName());
            pstmt.setString(2, customer.getLastName());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getPasswordHash());
            pstmt.setString(5, customer.getPhone());
            pstmt.setString(6, customer.getAddress());
            pstmt.setString(7, customer.getPanNumber());
            pstmt.setString(8, customer.getDateOfBirth());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
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
                return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching customer by email.");
            e.printStackTrace();
        }
        return null;
    }

    // 3. Method to fetch a customer by customer_id
    public Customer getCustomerById(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching customer by ID: " + customerId);
            e.printStackTrace();
        }
        return null;
    }

    // 4. Update customer profile details
    public boolean updateProfile(int customerId, String phone, String address, String panNumber, String dateOfBirth) {
        String sql = "UPDATE customers SET phone = ?, address = ?, pan_number = ?, date_of_birth = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setString(2, address);
            pstmt.setString(3, panNumber);
            pstmt.setString(4, dateOfBirth);
            pstmt.setInt(5, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Failed to update profile for customer ID: " + customerId);
            e.printStackTrace();
            return false;
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        String phone = null, address = null, panNumber = null, dateOfBirth = null;
        try { phone = rs.getString("phone"); } catch (SQLException ignored) {}
        try { address = rs.getString("address"); } catch (SQLException ignored) {}
        try { panNumber = rs.getString("pan_number"); } catch (SQLException ignored) {}
        try { dateOfBirth = rs.getString("date_of_birth"); } catch (SQLException ignored) {}

        return new Customer(
            rs.getInt("customer_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("password_hash"),
            phone,
            address,
            panNumber,
            dateOfBirth,
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
