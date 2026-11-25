package repository;

import config.DatabaseConnection;
import model.Account;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository {

    // 1. Open a new bank account (INSERT)
    public boolean saveAccount(Account account) {
        String sql = "INSERT INTO accounts (customer_id, account_type, balance) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, account.getCustomerId());
            pstmt.setString(2, account.getAccountType());
            pstmt.setDouble(3, account.getBalance());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.out.println("❌ Failed to save account.");
            e.printStackTrace();
            return false;
        }
    }

    // 1b. Create account and return the fully initialized Account with generated account_number
    public Account createAccount(int customerId, String accountType, double initialBalance) {
        String sql = "INSERT INTO accounts (customer_id, account_type, balance) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, customerId);
            pstmt.setString(2, accountType.toUpperCase());
            pstmt.setDouble(3, initialBalance);
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int accNumber = rs.getInt(1);
                        return getAccountByNumber(accNumber);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Failed to create account for customer " + customerId);
            e.printStackTrace();
        }
        return null;
    }

    // Format raw database account_number to real-world 10-digit commercial account number
    public static String formatAccountNumber(int accNum) {
        if (accNum < 10000) {
            return String.format("0723%04d04", accNum);
        }
        return String.format("0723%06d", accNum);
    }
    public boolean deleteAccount(int accountNumber, int customerId) {
        String sql = "DELETE FROM accounts WHERE account_number = ? AND customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            pstmt.setInt(2, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Failed to delete account #" + accountNumber);
            e.printStackTrace();
            return false;
        }
    }

    // 2. Fetch a single account by its Account Number (SELECT)
    public Account getAccountByNumber(int accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Account(
                    rs.getInt("account_number"),
                    rs.getInt("customer_id"),
                    rs.getString("account_type"),
                    rs.getDouble("balance"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching account #" + accountNumber);
            e.printStackTrace();
        }
        return null;
    }

    // 3. Fetch all accounts belonging to a specific customer (SELECT)
    public List<Account> getAccountsByCustomerId(int customerId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(new Account(
                    rs.getInt("account_number"),
                    rs.getInt("customer_id"),
                    rs.getString("account_type"),
                    rs.getDouble("balance"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching accounts for customer ID: " + customerId);
            e.printStackTrace();
        }
        return accounts;
    }

    // 4. Update an account's balance (UPDATE)
    public boolean updateBalance(int accountNumber, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, accountNumber);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.out.println("❌ Failed to update balance for account #" + accountNumber);
            e.printStackTrace();
            return false;
        }
    }
}
