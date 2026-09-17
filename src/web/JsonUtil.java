package web;

import model.Expense;
import model.UserProfile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tiny hand-rolled JSON helper. We deliberately avoid pulling in a library
 * like Gson/Jackson so the whole app stays dependency-free and compiles
 * with nothing but the JDK. Only supports the flat shapes this app needs:
 * arrays of Expense, string->double maps, string arrays, and flat request
 * objects with string/number fields.
 */
public final class JsonUtil {

    private JsonUtil() { }

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String expenseToJson(Expense e) {
        return "{"
                + "\"id\":" + e.getId() + ","
                + "\"title\":\"" + escape(e.getTitle()) + "\","
                + "\"amount\":" + e.getAmount() + ","
                + "\"category\":\"" + escape(e.getCategory()) + "\","
                + "\"date\":\"" + e.getFormattedDate() + "\","
                + "\"note\":\"" + escape(e.getNote()) + "\""
                + "}";
    }

    public static String expensesToJsonArray(List<Expense> expenses) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < expenses.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(expenseToJson(expenses.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String stringsToJsonArray(List<String> strings) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < strings.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(strings.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String mapToJsonObject(Map<String, Double> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(entry.getKey())).append("\":").append(entry.getValue());
        }
        sb.append("}");
        return sb.toString();
    }

    public static String profileToJson(UserProfile p) {
        if (p == null) return "{\"exists\":false}";
        return "{"
                + "\"exists\":true,"
                + "\"name\":\"" + escape(p.getName()) + "\","
                + "\"fullName\":\"" + escape(p.getFullName()) + "\","
                + "\"email\":\"" + escape(p.getEmail()) + "\","
                + "\"monthlyIncome\":" + p.getMonthlyIncome()
                + "}";
    }

    public static String errorJson(String message) {
        return "{\"error\":\"" + escape(message) + "\"}";
    }

    /**
     * Parses a FLAT JSON object (no nested objects/arrays) into a String->String map.
     * Good enough for our request bodies: {"title":"Coffee","amount":150,"category":"Food",...}
     * Numbers and strings both come back as their raw text form; the caller decides
     * how to convert each field.
     */
    public static Map<String, String> parseFlatObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        if (json == null) return result;
        String trimmed = json.trim();
        if (trimmed.isEmpty()) return result;
        if (trimmed.startsWith("{")) trimmed = trimmed.substring(1);
        if (trimmed.endsWith("}")) trimmed = trimmed.substring(0, trimmed.length() - 1);

        int i = 0;
        int n = trimmed.length();
        while (i < n) {
            // skip whitespace / commas
            while (i < n && (Character.isWhitespace(trimmed.charAt(i)) || trimmed.charAt(i) == ',')) i++;
            if (i >= n) break;

            // parse key (quoted string)
            if (trimmed.charAt(i) != '"') break;
            i++;
            StringBuilder key = new StringBuilder();
            while (i < n && trimmed.charAt(i) != '"') {
                char c = trimmed.charAt(i);
                if (c == '\\' && i + 1 < n) { i++; key.append(trimmed.charAt(i)); }
                else key.append(c);
                i++;
            }
            i++; // skip closing quote

            // skip whitespace and colon
            while (i < n && (Character.isWhitespace(trimmed.charAt(i)) || trimmed.charAt(i) == ':')) i++;

            // parse value: either a quoted string or a bare token (number/true/false/null)
            StringBuilder value = new StringBuilder();
            if (i < n && trimmed.charAt(i) == '"') {
                i++;
                while (i < n && trimmed.charAt(i) != '"') {
                    char c = trimmed.charAt(i);
                    if (c == '\\' && i + 1 < n) { i++; value.append(trimmed.charAt(i)); }
                    else value.append(c);
                    i++;
                }
                i++; // skip closing quote
            } else {
                while (i < n && trimmed.charAt(i) != ',' && trimmed.charAt(i) != '}') {
                    value.append(trimmed.charAt(i));
                    i++;
                }
            }
            result.put(key.toString(), value.toString().trim());
        }
        return result;
    }

    /** Parses query-string style params from a URI's raw query, e.g. "keyword=foo&n=3". */
    public static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> params = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) return params;
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) continue;
            String key = urlDecode(pair.substring(0, eq));
            String value = urlDecode(pair.substring(eq + 1));
            params.put(key, value);
        }
        return params;
    }

    private static String urlDecode(String s) {
        try {
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}
