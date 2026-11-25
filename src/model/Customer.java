package model;

import java.time.LocalDateTime;

public class Customer {
    // Private fields matching database columns
    private int customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash; // Stored securely
    private String phone;
    private String address;
    private String panNumber;    // Government / Tax ID (e.g. PAN)
    private String dateOfBirth;  // YYYY-MM-DD or DD/MM/YYYY
    private LocalDateTime createdAt;

    // Minimal constructor for backwards compatibility
    public Customer(String firstName, String lastName, String email, String passwordHash) {
        this(firstName, lastName, email, passwordHash, null, null, null, null);
    }

    // Full constructor for creating a new customer with profile
    public Customer(String firstName, String lastName, String email, String passwordHash,
                    String phone, String address, String panNumber, String dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.address = address;
        this.panNumber = panNumber;
        this.dateOfBirth = dateOfBirth;
    }

    // Constructor for fetching an existing customer out of the database (minimal)
    public Customer(int customerId, String firstName, String lastName, String email, String passwordHash, LocalDateTime createdAt) {
        this(customerId, firstName, lastName, email, passwordHash, null, null, null, null, createdAt);
    }

    // Full constructor for fetching an existing customer out of the database
    public Customer(int customerId, String firstName, String lastName, String email, String passwordHash,
                    String phone, String address, String panNumber, String dateOfBirth, LocalDateTime createdAt) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.address = address;
        this.panNumber = panNumber;
        this.dateOfBirth = dateOfBirth;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
