package server;

import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight JSON parser and serializer utilities using pure standard Java.
 */
public class JsonUtil {

    /**
     * Reads the entire body of an HttpExchange as a UTF-8 String.
     */
    public static String readBody(HttpExchange exchange) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString().trim();
    }

    /**
     * Extracts a string field from a JSON payload by key.
     * Example: for key "email" in {"email":"test@bank.com"}, returns "test@bank.com"
     */
    public static String getString(String json, String key) {
        if (json == null || key == null) return null;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return unescape(matcher.group(1));
        }
        return null;
    }

    /**
     * Extracts a numeric double field from a JSON payload.
     */
    public static Double getDouble(String json, String key) {
        if (json == null || key == null) return null;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Extracts a numeric integer field from a JSON payload.
     */
    public static Integer getInt(String json, String key) {
        if (json == null || key == null) return null;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Resolves 10-digit commercial bank account numbers (e.g. 0723100104) to internal DB IDs.
     */
    public static Integer normalizeAccountNumber(Integer accNum) {
        if (accNum == null) return null;
        String s = String.valueOf(accNum);
        if (s.startsWith("723") && s.length() == 9) {
            s = "0" + s;
        }
        if (s.startsWith("0723") && s.length() == 10) {
            try {
                if (s.endsWith("04")) {
                    return Integer.parseInt(s.substring(4, 8));
                }
                return Integer.parseInt(s.substring(4));
            } catch (NumberFormatException ignored) {}
        }
        return accNum;
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}
