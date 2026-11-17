package model;

import java.time.LocalDateTime;

public class Customer {
    // 1. Private fields matching database columns
    private int customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash; // Stored securely
    private LocalDateTime createdAt;

    // 2. Constructor for creating a brand new customer (before database assigns an ID)
    public Customer(String firstName, String lastName, String email, String passwordHash) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // 3. Constructor for fetching an existing customer out of the database
    public Customer(int customerId, String firstName, String lastName, String email, String passwordHash, LocalDateTime createdAt) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    // 4. Getters and Setters (Encapsulation)
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
