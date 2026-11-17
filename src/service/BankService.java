package service;

import config.DatabaseConnection;
import model.Account;
import model.Transaction;
import repository.AccountRepository;
import repository.TransactionRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BankService {
    private AccountRepository accountRepo = new AccountRepository();
    private TransactionRepository txRepo = new TransactionRepository();

    // 1. Deposit Money
    public boolean deposit(int accountNumber, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Deposit amount must be greater than zero.");
            return false;
        }

        Account account = accountRepo.getAccountByNumber(accountNumber);
        if (account == null) {
            System.out.println("❌ Account #" + accountNumber + " not found.");
            return false;
        }

        double newBalance = account.getBalance() + amount;
        if (accountRepo.updateBalance(accountNumber, newBalance)) {
            txRepo.saveTransaction(new Transaction(null, accountNumber, "DEPOSIT", amount));
            System.out.println("✅ Deposited $" + amount + " into Account #" + accountNumber);
            return true;
        }
        return false;
    }

    // 2. Withdraw Money
    public boolean withdraw(int accountNumber, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Withdrawal amount must be greater than zero.");
            return false;
        }

        Account account = accountRepo.getAccountByNumber(accountNumber);
        if (account == null) {
            System.out.println("❌ Account #" + accountNumber + " not found.");
            return false;
        }

        if (account.getBalance() < amount) {
            System.out.println("❌ Insufficient funds! Current balance: $" + account.getBalance());
            return false;
        }

        double newBalance = account.getBalance() - amount;
        if (accountRepo.updateBalance(accountNumber, newBalance)) {
            txRepo.saveTransaction(new Transaction(accountNumber, null, "WITHDRAWAL", amount));
            System.out.println("✅ Withdrew $" + amount + " from Account #" + accountNumber);
            return true;
        }
        return false;
    }

    // 3. Atomic Fund Transfer (All-or-Nothing using JDBC Transactions)
    public boolean transferFunds(int fromAccountNum, int toAccountNum, double amount) {
        if (amount <= 0 || fromAccountNum == toAccountNum) {
            System.out.println("❌ Invalid transfer parameters.");
            return false;
        }

        Account fromAcc = accountRepo.getAccountByNumber(fromAccountNum);
        Account toAcc = accountRepo.getAccountByNumber(toAccountNum);

        if (fromAcc == null || toAcc == null) {
            System.out.println("❌ One or both accounts do not exist.");
            return false;
        }

        if (fromAcc.getBalance() < amount) {
            System.out.println("❌ Insufficient funds for transfer.");
            return false;
        }

        String debitSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
        String creditSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
        String logSql = "INSERT INTO transactions (source_account_number, destination_account_number, transaction_type, amount) VALUES (?, ?, 'TRANSFER', ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Turn off auto-save so all 3 queries execute as one single atomic unit
            conn.setAutoCommit(false);

            try (PreparedStatement debitStmt = conn.prepareStatement(debitSql);
                 PreparedStatement creditStmt = conn.prepareStatement(creditSql);
                 PreparedStatement logStmt = conn.prepareStatement(logSql)) {

                // Step A: Deduct from sender
                debitStmt.setDouble(1, amount);
                debitStmt.setInt(2, fromAccountNum);
                debitStmt.executeUpdate();

                // Step B: Add to receiver
                creditStmt.setDouble(1, amount);
                creditStmt.setInt(2, toAccountNum);
                creditStmt.executeUpdate();

                // Step C: Log the transfer
                logStmt.setInt(1, fromAccountNum);
                logStmt.setInt(2, toAccountNum);
                logStmt.setDouble(3, amount);
                logStmt.executeUpdate();

                // Commit all changes permanently
                conn.commit();
                System.out.println("✅ Transferred $" + amount + " from #" + fromAccountNum + " to #" + toAccountNum);
                return true;

            } catch (SQLException e) {
                // Undo everything if any step fails
                conn.rollback();
                System.out.println("❌ Transfer failed! Rolled back all changes.");
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
