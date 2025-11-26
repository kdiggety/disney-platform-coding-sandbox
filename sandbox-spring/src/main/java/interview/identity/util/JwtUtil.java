package interview.identity.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class JwtUtil {
    private JwtUtil() {}

    public static String extractSubWithoutVerification(String token) {
        // Intentionally flawed: does not validate JWT structure robustly.
        String[] parts = token.split("\\.");
        if (parts.length < 2) throw new IllegalArgumentException("not a jwt");

        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

        // Intentionally flawed: naive JSON parsing (breaks easily)
        int idx = payloadJson.indexOf("\"sub\"");
        if (idx < 0) throw new IllegalArgumentException("missing sub");
        int colon = payloadJson.indexOf(":", idx);
        int firstQuote = payloadJson.indexOf("\"", colon);
        int secondQuote = payloadJson.indexOf("\"", firstQuote + 1);

        return payloadJson.substring(firstQuote + 1, secondQuote);
    }
}
