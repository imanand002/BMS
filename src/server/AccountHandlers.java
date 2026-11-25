package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Account;
import model.Customer;
import model.Transaction;
import repository.AccountRepository;
import repository.CustomerRepository;
import repository.TransactionRepository;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class AccountHandlers {
    private static final AccountRepository accountRepo = new AccountRepository();
    private static final CustomerRepository customerRepo = new CustomerRepository();
    private static final TransactionRepository txRepo = new TransactionRepository();

    /**
     * Handles /api/accounts for:
     * - GET: List all accounts of the logged-in customer
     * - POST: Open a new SAVINGS account
     * - DELETE: Close an account
     */
    public static class AccountsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Customer customer = SessionManager.getCustomerFromExchange(exchange);
            if (customer == null) {
                BankHttpServer.sendResponse(exchange, 401, "{\"error\": \"Unauthorized. Please login first.\"}");
                return;
            }

            String method = exchange.getRequestMethod().toUpperCase();
            switch (method) {
                case "GET" -> handleListAccounts(exchange, customer);
                case "POST" -> handleCreateAccount(exchange, customer);
                case "DELETE" -> handleDeleteAccount(exchange, customer);
                default -> BankHttpServer.sendResponse(exchange, 405, "{\"error\": \"Method not allowed.\"}");
            }
        }

        private void handleListAccounts(HttpExchange exchange, Customer customer) throws IOException {
            List<Account> accounts = accountRepo.getAccountsByCustomerId(customer.getCustomerId());

            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\": true, \"accounts\": [");
            for (int i = 0; i < accounts.size(); i++) {
                Account acc = accounts.get(i);
                String formatted = AccountRepository.formatAccountNumber(acc.getAccountNumber());
                sb.append(String.format(
                    "{\"number\": %d, \"accountNumber\": %d, \"displayAccountNumber\": \"%s\", \"type\": \"SAVINGS\", \"balance\": %.2f, \"createdAt\": \"%s\"}",
                    acc.getAccountNumber(),
                    acc.getAccountNumber(),
                    formatted,
                    acc.getBalance(),
                    acc.getCreatedAt() != null ? acc.getCreatedAt().toString() : ""
                ));
                if (i < accounts.size() - 1) sb.append(",");
            }
            sb.append("]}");

            BankHttpServer.sendResponse(exchange, 200, sb.toString());
        }

        private void handleCreateAccount(HttpExchange exchange, Customer customer) throws IOException {
            String body = JsonUtil.readBody(exchange);
            Double initialDeposit = JsonUtil.getDouble(body, "initialDeposit");

            double depositAmount = (initialDeposit != null) ? initialDeposit : 0.0;
            if (depositAmount < 0) {
                BankHttpServer.sendResponse(exchange, 400, "{\"error\": \"initialDeposit cannot be negative.\"}");
                return;
            }

            // Always create a SAVINGS account (requirement 2)
            Account created = accountRepo.createAccount(customer.getCustomerId(), "SAVINGS", depositAmount);
            if (created == null) {
                BankHttpServer.sendResponse(exchange, 500, "{\"error\": \"Failed to create account in database.\"}");
                return;
            }

            // Log initial deposit if > 0
            if (depositAmount > 0) {
                txRepo.saveTransaction(new Transaction(null, created.getAccountNumber(), "DEPOSIT", depositAmount));
            }

            String response = String.format(
                "{\"success\": true, \"message\": \"Savings account opened successfully\", \"account\": {\"number\": %d, \"accountNumber\": %d, \"type\": \"SAVINGS\", \"balance\": %.2f}}",
                created.getAccountNumber(),
                created.getAccountNumber(),
                created.getBalance()
            );

            BankHttpServer.sendResponse(exchange, 201, response);
        }

        private void handleDeleteAccount(HttpExchange exchange, Customer customer) throws IOException {
            String body = JsonUtil.readBody(exchange);
            Integer accNum = JsonUtil.normalizeAccountNumber(JsonUtil.getInt(body, "accountNumber"));

            if (accNum == null) {
                BankHttpServer.sendResponse(exchange, 400, "{\"error\": \"accountNumber is required.\"}");
                return;
            }

            Account existing = accountRepo.getAccountByNumber(accNum);
            if (existing == null || existing.getCustomerId() != customer.getCustomerId()) {
                BankHttpServer.sendResponse(exchange, 404, "{\"error\": \"Account not found or does not belong to you.\"}");
                return;
            }

            if (existing.getBalance() > 0) {
                BankHttpServer.sendResponse(exchange, 400, "{\"error\": \"Cannot close account with a remaining balance. Please withdraw or transfer all funds first.\"}");
                return;
            }

            boolean deleted = accountRepo.deleteAccount(accNum, customer.getCustomerId());
            if (deleted) {
                BankHttpServer.sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Account closed successfully.\"}");
            } else {
                BankHttpServer.sendResponse(exchange, 500, "{\"error\": \"Failed to close account.\"}");
            }
        }
    }

    /**
     * GET /api/accounts/lookup?number=1002
     * Used by Transfer UI to verify beneficiary existence and display their name.
     */
    public static class AccountLookupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Customer customer = SessionManager.getCustomerFromExchange(exchange);
            if (customer == null) {
                BankHttpServer.sendResponse(exchange, 401, "{\"error\": \"Unauthorized. Please login first.\"}");
                return;
            }

            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            Integer accNum = null;

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && ("number".equalsIgnoreCase(pair[0]) || "accountNumber".equalsIgnoreCase(pair[0]))) {
                        try {
                            accNum = JsonUtil.normalizeAccountNumber(Integer.parseInt(pair[1].trim()));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            if (accNum == null) {
                BankHttpServer.sendResponse(exchange, 400, "{\"error\": \"Valid account number query param is required.\"}");
                return;
            }

            Account account = accountRepo.getAccountByNumber(accNum);
            if (account == null) {
                BankHttpServer.sendResponse(exchange, 404, "{\"exists\": false, \"error\": \"Beneficiary account not found.\"}");
                return;
            }

            Customer holder = customerRepo.getCustomerById(account.getCustomerId());
            String holderName = (holder != null) ? holder.getFirstName() + " " + holder.getLastName() : "Verified Account Holder";
            boolean isOwn = (holder != null && holder.getCustomerId() == customer.getCustomerId());
            String formattedAcc = AccountRepository.formatAccountNumber(account.getAccountNumber());

            String response = String.format(
                "{\"exists\": true, \"accountNumber\": %d, \"displayAccountNumber\": \"%s\", \"type\": \"SAVINGS\", \"holderName\": \"%s\", \"isOwnAccount\": %b}",
                account.getAccountNumber(),
                formattedAcc,
                JsonUtil.escape(holderName),
                isOwn
            );

            BankHttpServer.sendResponse(exchange, 200, response);
        }
    }
}
