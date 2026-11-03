import java.time.LocalDateTime;

public class Transaction {
    private int transactionId;
    private Integer sourceAccountNumber;      // Can be null for direct deposits
    private Integer destinationAccountNumber; // Can be null for direct withdrawals
    private String transactionType;           // 'DEPOSIT', 'WITHDRAWAL', 'TRANSFER'
    private double amount;
    private LocalDateTime timestamp;

    // Constructor for logging a new transaction
    public Transaction(Integer sourceAccountNumber, Integer destinationAccountNumber, String transactionType, double amount) {
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
    }

    // Constructor for pulling past history from the database
    public Transaction(int transactionId, Integer sourceAccountNumber, Integer destinationAccountNumber, String transactionType, double amount, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public Integer getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(Integer sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }

    public Integer getDestinationAccountNumber() { return destinationAccountNumber; }
    public void setDestinationAccountNumber(Integer destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
