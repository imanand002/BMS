package server;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Filter that attaches CORS headers to every HTTP response
 * and automatically responds to preflight OPTIONS requests.
 */
public class CorsFilter extends Filter {

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        // Allow cross-origin requests from any frontend (e.g., http://localhost:5173)
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Preflight requests sent by browsers before POST/PUT or custom headers
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // 204 No Content
            return;
        }

        // Proceed to the actual endpoint handler
        chain.doFilter(exchange);
    }

    @Override
    public String description() {
        return "CORS filter to allow cross-origin requests from frontend";
    }
}
