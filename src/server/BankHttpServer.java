package server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class BankHttpServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        CorsFilter corsFilter = new CorsFilter();

        // Register endpoints with CORS filter attached
        registerRoute(server, "/api/health", new HealthHandler(), corsFilter);
        registerRoute(server, "/api/register", new AuthHandlers.RegisterHandler(), corsFilter);
        registerRoute(server, "/api/login", new AuthHandlers.LoginHandler(), corsFilter);
        registerRoute(server, "/api/logout", new AuthHandlers.LogoutHandler(), corsFilter);
        registerRoute(server, "/api/me", new AuthHandlers.MeHandler(), corsFilter);
        registerRoute(server, "/api/profile", new AuthHandlers.ProfileHandler(), corsFilter);
        registerRoute(server, "/api/accounts/lookup", new AccountHandlers.AccountLookupHandler(), corsFilter);
        registerRoute(server, "/api/accounts", new AccountHandlers.AccountsHandler(), corsFilter);
        registerRoute(server, "/api/transactions/deposit", new TransactionHandlers.DepositHandler(), corsFilter);
        registerRoute(server, "/api/transactions/withdraw", new TransactionHandlers.WithdrawHandler(), corsFilter);
        registerRoute(server, "/api/transactions/transfer", new TransactionHandlers.TransferHandler(), corsFilter);
        registerRoute(server, "/api/transactions", new TransactionHandlers.TransactionHistoryHandler(), corsFilter);

        // Multi-threaded executor to handle concurrent requests (10 threads)
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("==================================================");
        System.out.println("  Bank Management HTTP Server is running!     ");
        System.out.println("  Listening on: http://localhost:" + PORT);
        System.out.println("  Available Endpoints:");
        System.out.println("    - GET    /api/health");
        System.out.println("    - POST   /api/register");
        System.out.println("    - POST   /api/login");
        System.out.println("    - POST   /api/logout");
        System.out.println("    - GET    /api/me");
        System.out.println("    - POST   /api/profile");
        System.out.println("    - GET    /api/accounts");
        System.out.println("    - GET    /api/accounts/lookup");
        System.out.println("    - POST   /api/accounts");
        System.out.println("    - DELETE /api/accounts");
        System.out.println("    - POST   /api/transactions/deposit");
        System.out.println("    - POST   /api/transactions/withdraw");
        System.out.println("    - POST   /api/transactions/transfer");
        System.out.println("    - GET    /api/transactions");
        System.out.println("==================================================");
    }

    private static void registerRoute(HttpServer server, String path, HttpHandler handler, CorsFilter corsFilter) {
        HttpContext context = server.createContext(path, handler);
        context.getFilters().add(corsFilter);
    }

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }
            sendResponse(exchange, 200, "{\"status\": \"UP\", \"message\": \"Bank Management API is running\"}");
        }
    }

    /**
     * Helper to write JSON responses with proper headers
     */
    public static void sendResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
