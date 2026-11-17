import model.Account;
import model.Customer;
import model.Transaction;
import repository.AccountRepository;
import repository.CustomerRepository;
import repository.TransactionRepository;
import service.BankService;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final CustomerRepository customerRepo = new CustomerRepository();
    private static final AccountRepository accountRepo = new AccountRepository();
    private static final TransactionRepository txRepo = new TransactionRepository();
    private static final BankService bankService = new BankService();

    private static Customer loggedInCustomer = null;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   WELCOME TO Rahul Modi Cooperative BANK CLI   ");
        System.out.println("=========================================");

        while (true) {
            if (loggedInCustomer == null) {
                showAuthMenu();
            } else {
                showBankingMenu();
            }
        }
    }

    private static void showAuthMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Register New Customer");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");

        int choice = readInt();
        switch (choice) {
            case 1 -> handleRegister();
            case 2 -> handleLogin();
            case 3 -> {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Invalid option. Try again.");
        }
    }

    private static void showBankingMenu() {
        System.out.println("\n--- DASHBOARD (" + loggedInCustomer.getFirstName() + ") ---");
        System.out.println("1. Open New Account");
        System.out.println("2. View My Accounts & Balances");
        System.out.println("3. Deposit Money");
        System.out.println("4. Withdraw Money");
        System.out.println("5. Transfer Funds");
        System.out.println("6. View Mini Statement");
        System.out.println("7. Logout");
        System.out.print("Choose an option: ");

        int choice = readInt();
        switch (choice) {
            case 1 -> handleOpenAccount();
            case 2 -> handleViewAccounts();
            case 3 -> handleDeposit();
            case 4 -> handleWithdraw();
            case 5 -> handleTransfer();
            case 6 -> handleStatement();
            case 7 -> {
                loggedInCustomer = null;
                System.out.println("Logged out successfully.");
            }
            default -> System.out.println("Invalid option. Try again.");
        }
    }

    private static void handleRegister() {
        System.out.print("First Name: ");
        String first = scanner.nextLine();
        System.out.print("Last Name: ");
        String last = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        Customer newCust = new Customer(first, last, email, pass);
        if (customerRepo.saveCustomer(newCust)) {
            System.out.println("Registration successful! You can now log in.");
        }
    }

    private static void handleLogin() {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        Customer cust = customerRepo.getCustomerByEmail(email);
        if (cust != null && cust.getPasswordHash().equals(pass)) {
            loggedInCustomer = cust;
            System.out.println("Welcome back, " + cust.getFirstName() + "!");
        } else {
            System.out.println("Invalid email or password.");
        }
    }

    private static void handleOpenAccount() {
        System.out.print("Account Type (CHECKING or SAVINGS): ");
        String type = scanner.nextLine().trim().toUpperCase();
        if (!type.equals("CHECKING") && !type.equals("SAVINGS")) {
            System.out.println("Invalid type. Must be CHECKING or SAVINGS.");
            return;
        }
        System.out.print("Initial Deposit Amount: ");
        double initial = readDouble();

        Account acc = new Account(loggedInCustomer.getCustomerId(), type, initial);
        if (accountRepo.saveAccount(acc)) {
            System.out.println("✅ " + type + " account opened successfully!");
        }
    }

    private static void handleViewAccounts() {
        List<Account> accounts = accountRepo.getAccountsByCustomerId(loggedInCustomer.getCustomerId());
        if (accounts.isEmpty()) {
            System.out.println("You have no active accounts yet.");
            return;
        }
        System.out.println("\n--- YOUR ACCOUNTS ---");
        for (Account acc : accounts) {
            System.out.printf("Account #%d | %-8s | Balance: $%.2f%n",
                    acc.getAccountNumber(), acc.getAccountType(), acc.getBalance());
        }
    }

    private static void handleDeposit() {
        System.out.print("Enter Account Number: ");
        int accNum = readInt();
        System.out.print("Enter Deposit Amount: ");
        double amount = readDouble();
        bankService.deposit(accNum, amount);
    }

    private static void handleWithdraw() {
        System.out.print("Enter Account Number: ");
        int accNum = readInt();
        System.out.print("Enter Withdrawal Amount: ");
        double amount = readDouble();
        bankService.withdraw(accNum, amount);
    }

    private static void handleTransfer() {
        System.out.print("From Account Number: ");
        int fromAcc = readInt();
        System.out.print("To Account Number: ");
        int toAcc = readInt();
        System.out.print("Amount to Transfer: ");
        double amount = readDouble();
        bankService.transferFunds(fromAcc, toAcc, amount);
    }

    private static void handleStatement() {
        System.out.print("Enter Account Number: ");
        int accNum = readInt();
        List<Transaction> list = txRepo.getTransactionsByAccount(accNum);
        System.out.println("\n--- MINI STATEMENT (#" + accNum + ") ---");
        for (Transaction tx : list) {
            System.out.printf("%s | %-10s | $%.2f%n",
                    tx.getTimestamp().toLocalDate(), tx.getTransactionType(), tx.getAmount());
        }
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid amount: ");
            }
        }
    }
}
