package server;

import com.sun.net.httpserver.HttpExchange;
import model.Customer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active user login sessions in-memory.
 * Maps random session tokens (UUIDs) to authenticated Customer models.
 */
public class SessionManager {
    private static final Map<String, Customer> activeSessions = new ConcurrentHashMap<>();

    public static String createSession(Customer customer) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, customer);
        return token;
    }

    public static Customer getCustomer(String token) {
        if (token == null || token.isBlank()) return null;
        return activeSessions.get(token);
    }

    public static boolean removeSession(String token) {
        if (token == null || token.isBlank()) return false;
        return activeSessions.remove(token) != null;
    }

    /**
     * Extracts token from "Authorization: Bearer <token>" or "X-Auth-Token" header.
     */
    public static Customer getCustomerFromExchange(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        } else {
            token = exchange.getRequestHeaders().getFirst("X-Auth-Token");
        }

        return getCustomer(token);
    }

    public static String getTokenFromExchange(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return exchange.getRequestHeaders().getFirst("X-Auth-Token");
    }
}
