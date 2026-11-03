
import java.time.LocalDateTime;

public class Account {
    private int accountNumber;
    private int customerId;      // Links this account to a specific customer
    private String accountType;  // 'CHECKING' or 'SAVINGS'
    private double balance;
    private LocalDateTime createdAt;

    // Constructor for opening a fresh account
    public Account(int customerId, String accountType, double initialBalance) {
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = initialBalance;
    }

    // Constructor for loading an existing account from the database
    public Account(int accountNumber, int customerId, String accountType, double balance, LocalDateTime createdAt) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getAccountNumber() { return accountNumber; }
    public void setAccountNumber(int accountNumber) { this.accountNumber = accountNumber; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
