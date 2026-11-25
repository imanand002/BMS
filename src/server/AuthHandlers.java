package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Customer;
import repository.CustomerRepository;

import java.io.IOException;

public class AuthHandlers {
    private static final CustomerRepository customerRepo = new CustomerRepository();

    public static String formatCustomerJson(Customer c) {
        if (c == null) return "{}";
        return String.format(
            "{\"id\": %d, \"customerId\": %d, \"firstName\": \"%s\", \"lastName\": \"%s\", \"email\": \"%s\", \"phone\": \"%s\", \"address\": \"%s\", \"panNumber\": \"%s\", \"dateOfBirth\": \"%s\", \"createdAt\": \"%s\"}",
            c.getCustomerId(),
            c.getCustomerId(),
            JsonUtil.escape(c.getFirstName()),
            JsonUtil.escape(c.getLastName()),
            JsonUtil.escape(c.getEmail()),
            JsonUtil.escape(c.getPhone() != null ? c.getPhone() : ""),
            JsonUtil.escape(c.getAddress() != null ? c.getAddress() : ""),
            JsonUtil.escape(c.getPanNumber() != null ? c.getPanNumber() : ""),
            JsonUtil.escape(c.getDateOfBirth() != null ? c.getDateOfBirth() : ""),
            c.getCreatedAt() != null ? c.getCreatedAt().toString() : ""
        );
    }

    /**
     * POST /api/register
     */
    public static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                BankHttpServer.sendResponse(exchange, 405, "{\"error\": \"Method not allowed. Use POST.\"}");
                return;
            }

            String body = JsonUtil.readBody(exchange);
            String firstName = JsonUtil.getString(body, "firstName");
            String lastName = JsonUtil.getString(body, "lastName");
            String email = JsonUtil.getString(body, "email");
            String password = JsonUtil.getString(body, "password");
            String phone = JsonUtil.getString(body, "phone");
            String address = JsonUtil.getString(body, "address");
            String panNumber = JsonUtil.getString(body, "panNumber");
            String dateOfBirth = JsonUtil.getString(body, "dateOfBirth");

            if (firstName == null || firstName.isBlank() ||
                lastName == null || lastName.isBlank() ||
                email == null || email.isBlank() ||
                password == null || password.isBlank()) {
                BankHttpServer.sendResponse(exchange, 400, "{\"error\": \"Name, email, and password are required.\"}");
                return;
            }

            // Check if email is already registered
            Customer existing = customerRepo.getCustomerByEmail(email.trim().toLowerCase());
            if (existing != null) {
                BankHttpServer.sendResponse(exchange, 409, "{\"error\": \"An account with this email already exists.\"}");
                return;
            }

            Customer newCustomer = new Customer(
                firstName.trim(),
                lastName.trim(),
                email.trim().toLowerCase(),
                password,
                phone != null ? phone.trim() : "",
                address != null ? address.trim() : "",
                panNumber != null ? panNumber.trim().toUpperCase() : "",
                dateOfBirth != null ? dateOfBirth.trim() : ""
            );

            boolean saved = customerRepo.saveCustomer(newCustomer);
            if (!saved) {
                BankHttpServer.sendResponse(exchange, 500, "{\"error\": \"Failed to register customer in database.\"}");
                return;
            }

            Customer created = customerRepo.getCustomerByEmail(email.trim().toLowerCase());
            String token = SessionManager.createSession(created);

            String response = String.format(
                "{\"success\": true, \"message\": \"Registration successful\", \"token\": \"%s\", \"customer\": %s}",
                token,
                formatCustomerJson(created)
            );

            BankHttpServer.sendResponse(exchange, 201, response);
        }
    }

    /**
     * POST /api/login
     */
    public static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                BankHttpServer.sendResponse(exchange, 405, "{\"error\": \"Method not allowed. Use POST.\"}");
                return;
            }

            String body = JsonUtil.readBody(exchange);
            String email = JsonUtil.getString(body, "email");
            String password = JsonUtil.getString(body, "password");

            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                BankHttpServer.sendResponse(exchange, 400, "{\"error\": \"Email and password are required.\"}");
                return;
            }

            Customer customer = customerRepo.getCustomerByEmail(email.trim().toLowerCase());
            if (customer == null || !customer.getPasswordHash().equals(password)) {
                BankHttpServer.sendResponse(exchange, 401, "{\"error\": \"Invalid email or password.\"}");
                return;
            }

            String token = SessionManager.createSession(customer);

            String response = String.format(
                "{\"success\": true, \"message\": \"Login successful\", \"token\": \"%s\", \"customer\": %s}",
                token,
                formatCustomerJson(customer)
            );

            BankHttpServer.sendResponse(exchange, 200, response);
        }
    }

    /**
     * POST /api/logout
     */
    public static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                BankHttpServer.sendResponse(exchange, 405, "{\"error\": \"Method not allowed. Use POST.\"}");
                return;
            }

            String token = SessionManager.getTokenFromExchange(exchange);
            SessionManager.removeSession(token);

            BankHttpServer.sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Logged out successfully.\"}");
        }
    }

    /**
     * GET /api/me
     */
    public static class MeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                BankHttpServer.sendResponse(exchange, 405, "{\"error\": \"Method not allowed. Use GET.\"}");
                return;
            }

            Customer sessionCustomer = SessionManager.getCustomerFromExchange(exchange);
            if (sessionCustomer == null) {
                BankHttpServer.sendResponse(exchange, 401, "{\"authenticated\": false, \"error\": \"Unauthorized. Invalid or expired token.\"}");
                return;
            }

            // Fresh read from database
            Customer customer = customerRepo.getCustomerById(sessionCustomer.getCustomerId());
            if (customer == null) customer = sessionCustomer;

            String response = String.format(
                "{\"authenticated\": true, \"customer\": %s}",
                formatCustomerJson(customer)
            );

            BankHttpServer.sendResponse(exchange, 200, response);
        }
    }

    /**
     * POST /api/profile
     * Update customer profile data (phone, address, panNumber, dateOfBirth)
     */
    public static class ProfileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Customer customer = SessionManager.getCustomerFromExchange(exchange);
            if (customer == null) {
                BankHttpServer.sendResponse(exchange, 401, "{\"error\": \"Unauthorized. Please login first.\"}");
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()) && !"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                BankHttpServer.sendResponse(exchange, 405, "{\"error\": \"Method not allowed. Use POST or PUT.\"}");
                return;
            }

            String body = JsonUtil.readBody(exchange);
            String phone = JsonUtil.getString(body, "phone");
            String address = JsonUtil.getString(body, "address");
            String panNumber = JsonUtil.getString(body, "panNumber");
            String dateOfBirth = JsonUtil.getString(body, "dateOfBirth");

            boolean updated = customerRepo.updateProfile(
                customer.getCustomerId(),
                phone != null ? phone.trim() : customer.getPhone(),
                address != null ? address.trim() : customer.getAddress(),
                panNumber != null ? panNumber.trim().toUpperCase() : customer.getPanNumber(),
                dateOfBirth != null ? dateOfBirth.trim() : customer.getDateOfBirth()
            );

            if (!updated) {
                BankHttpServer.sendResponse(exchange, 500, "{\"error\": \"Failed to update profile in database.\"}");
                return;
            }

            Customer fresh = customerRepo.getCustomerById(customer.getCustomerId());
            String response = String.format(
                "{\"success\": true, \"message\": \"Profile updated successfully\", \"customer\": %s}",
                formatCustomerJson(fresh)
            );

            BankHttpServer.sendResponse(exchange, 200, response);
        }
    }
}
