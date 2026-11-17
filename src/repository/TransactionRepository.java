package repository;

import config.DatabaseConnection;
import model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    // 1. Log a new transaction into the database
    public boolean saveTransaction(Transaction tx) {
        String sql = "INSERT INTO transactions (source_account_number, destination_account_number, transaction_type, amount) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Handle null source account (e.g., cash deposit)
            if (tx.getSourceAccountNumber() != null) {
                pstmt.setInt(1, tx.getSourceAccountNumber());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }

            // Handle null destination account (e.g., cash withdrawal)
            if (tx.getDestinationAccountNumber() != null) {
                pstmt.setInt(2, tx.getDestinationAccountNumber());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            pstmt.setString(3, tx.getTransactionType());
            pstmt.setDouble(4, tx.getAmount());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.out.println("❌ Failed to log transaction.");
            e.printStackTrace();
            return false;
        }
    }

    // 2. Fetch transaction history for a specific account
    public List<Transaction> getTransactionsByAccount(int accountNumber) {
        List<Transaction> history = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE source_account_number = ? OR destination_account_number = ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, accountNumber);
            pstmt.setInt(2, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                // getObject(..., Integer.class) safely reads NULL values from the database
                Integer sourceAcc = rs.getObject("source_account_number", Integer.class);
                Integer destAcc = rs.getObject("destination_account_number", Integer.class);

                history.add(new Transaction(
                    rs.getInt("transaction_id"),
                    sourceAcc,
                    destAcc,
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("timestamp").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching transaction history.");
            e.printStackTrace();
        }
        return history;
    }
}
