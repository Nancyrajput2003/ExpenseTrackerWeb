package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import core.ExpenseManager;
import core.ProfileManager;
import model.Expense;
import model.UserProfile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Minimal REST API + static file server built entirely on the JDK's
 * built-in com.sun.net.httpserver.HttpServer (no Spring, no external
 * dependencies) so the whole app compiles and runs with nothing but
 * `javac`/`java`.
 *
 * Every endpoint maps 1:1 to a DSA-backed method on ExpenseManager —
 * see README.md for the full mapping.
 */
public class ApiServer {

    private final ExpenseManager manager;
    private final ProfileManager profileManager;
    private final File webRoot;
    private HttpServer server;

    public ApiServer(ExpenseManager manager, ProfileManager profileManager, File webRoot) {
        this.manager = manager;
        this.profileManager = profileManager;
        this.webRoot = webRoot;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/expenses/period", this::handleExpensePeriod);
        server.createContext("/api/expenses", this::handleExpenses);
        server.createContext("/api/sort", this::handleSort);
        server.createContext("/api/search/date", this::handleSearchByDate);
        server.createContext("/api/search", this::handleSearchByKeyword);
        server.createContext("/api/categories", this::handleCategories);
        server.createContext("/api/top", this::handleTopExpenses);
        server.createContext("/api/activity", this::handleActivity);
        server.createContext("/api/reminders/process", this::handleReminderProcess);
        server.createContext("/api/reminders/count", this::handleReminderCount);
        server.createContext("/api/reminders", this::handleReminderSchedule);
        server.createContext("/api/stats/today", this::handleTodayStat);
        server.createContext("/api/profile/income", this::handleProfileIncome);
        server.createContext("/api/profile", this::handleProfile);
        server.createContext("/", this::handleStatic);

        server.setExecutor(null); // default single-threaded executor is fine for a demo app
        server.start();
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    // ---------------------- /api/expenses : GET (list), POST (add), DELETE (remove) ----------------------

    private void handleExpenses(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (method.equals("GET") && path.equals("/api/expenses")) {
                List<Expense> all = manager.getAll();
                sendJson(exchange, 200, JsonUtil.expensesToJsonArray(all));

            } else if (method.equals("POST") && path.equals("/api/expenses")) {
                Map<String, String> body = JsonUtil.parseFlatObject(readBody(exchange));
                String title = body.getOrDefault("title", "").trim();
                String amountStr = body.getOrDefault("amount", "");
                String category = body.getOrDefault("category", "").trim();
                String dateStr = body.getOrDefault("date", "").trim();
                String note = body.getOrDefault("note", "").trim();

                if (title.isEmpty()) {
                    sendJson(exchange, 400, JsonUtil.errorJson("Title is required"));
                    return;
                }
                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    sendJson(exchange, 400, JsonUtil.errorJson("Amount must be a number"));
                    return;
                }
                if (category.isEmpty()) category = "Other";
                LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);

                Expense created = manager.addExpense(title, amount, category, date, note);
                sendJson(exchange, 201, JsonUtil.expenseToJson(created));

            } else if (method.equals("DELETE") && path.startsWith("/api/expenses/")) {
                String idStr = path.substring("/api/expenses/".length());
                int id = Integer.parseInt(idStr);
                boolean removed = manager.deleteExpense(id);
                if (removed) sendJson(exchange, 200, "{\"deleted\":true}");
                else sendJson(exchange, 404, JsonUtil.errorJson("No expense with id " + id));

            } else {
                sendJson(exchange, 404, JsonUtil.errorJson("Not found"));
            }
        } catch (Exception e) {
            sendJson(exchange, 500, JsonUtil.errorJson("Server error: " + e.getMessage()));
        }
    }

    // ---------------------- /api/sort : POST (QuickSort / MergeSort) ----------------------

    private void handleSort(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            sendJson(exchange, 405, JsonUtil.errorJson("Use POST"));
            return;
        }
        try {
            Map<String, String> params = JsonUtil.parseQuery(exchange.getRequestURI().getQuery());
            String field = params.getOrDefault("field", "date");
            String algo = params.getOrDefault("algo", "merge");
            boolean useQuickSort = algo.equalsIgnoreCase("quick");

            switch (field.toLowerCase()) {
                case "amount": manager.sortByAmount(useQuickSort); break;
                case "category": manager.sortByCategory(useQuickSort); break;
                default: manager.sortByDate(useQuickSort); break;
            }
            sendJson(exchange, 200, JsonUtil.expensesToJsonArray(manager.getAll()));
        } catch (Exception e) {
            sendJson(exchange, 500, JsonUtil.errorJson("Server error: " + e.getMessage()));
        }
    }

    // ---------------------- /api/search : GET (Linear Search) ----------------------

    private void handleSearchByKeyword(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendJson(exchange, 405, JsonUtil.errorJson("Use GET"));
            return;
        }
        Map<String, String> params = JsonUtil.parseQuery(exchange.getRequestURI().getQuery());
        String keyword = params.getOrDefault("keyword", "");
        List<Expense> results = manager.searchByKeyword(keyword);
        sendJson(exchange, 200, JsonUtil.expensesToJsonArray(results));
    }

    // ---------------------- /api/search/date : GET (Binary Search) ----------------------

    private void handleSearchByDate(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendJson(exchange, 405, JsonUtil.errorJson("Use GET"));
            return;
        }
        try {
            Map<String, String> params = JsonUtil.parseQuery(exchange.getRequestURI().getQuery());
            String dateStr = params.getOrDefault("date", "");
            LocalDate date = LocalDate.parse(dateStr);
            List<Expense> results = manager.searchByDate(date);
            sendJson(exchange, 200, JsonUtil.expensesToJsonArray(results));
        } catch (Exception e) {
            sendJson(exchange, 400, JsonUtil.errorJson("Invalid or missing date (expected yyyy-MM-dd)"));
        }
    }

    // ---------------------- /api/expenses/period : GET (Monthly / Yearly view, Linear Scan) ----------------------

    private void handleExpensePeriod(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendJson(exchange, 405, JsonUtil.errorJson("Use GET"));
            return;
        }
        try {
            Map<String, String> params = JsonUtil.parseQuery(exchange.getRequestURI().getQuery());
            int year = Integer.parseInt(params.getOrDefault("year", String.valueOf(LocalDate.now().getYear())));
            String monthParam = params.get("month");

            List<Expense> results;
            if (monthParam != null && !monthParam.isEmpty()) {
                int month = Integer.parseInt(monthParam);
                results = manager.getExpensesForMonth(year, month);
            } else {
                results = manager.getExpensesForYear(year);
            }

            double total = 0;
            for (Expense e : results) total += e.getAmount();

            String json = "{\"expenses\":" + JsonUtil.expensesToJsonArray(results) + ",\"total\":" + total + "}";
            sendJson(exchange, 200, json);
        } catch (Exception e) {
            sendJson(exchange, 400, JsonUtil.errorJson("Invalid year/month"));
        }
    }

    // ---------------------- /api/stats/today : GET (Linear Scan) ----------------------

    private void handleTodayStat(HttpExchange exchange) throws IOException {
        sendJson(exchange, 200, "{\"total\":" + manager.getTodayTotal() + "}");
    }

    // ---------------------- /api/profile : GET / POST / DELETE ----------------------

    private void handleProfile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (!path.equals("/api/profile")) {
            sendJson(exchange, 404, JsonUtil.errorJson("Not found"));
            return;
        }
        String method = exchange.getRequestMethod();
        try {
            if (method.equals("GET")) {
                sendJson(exchange, 200, JsonUtil.profileToJson(profileManager.get()));

            } else if (method.equals("POST")) {
                Map<String, String> body = JsonUtil.parseFlatObject(readBody(exchange));
                String name = body.getOrDefault("name", "").trim();
                String fullName = body.getOrDefault("fullName", "").trim();
                String email = body.getOrDefault("email", "").trim();

                if (fullName.isEmpty()) {
                    sendJson(exchange, 400, JsonUtil.errorJson("Full name is required"));
                    return;
                }
                UserProfile saved = profileManager.createOrUpdate(name, fullName, email);
                sendJson(exchange, 201, JsonUtil.profileToJson(saved));

            } else if (method.equals("DELETE")) {
                profileManager.deleteAccount();
                manager.clearAll();
                sendJson(exchange, 200, "{\"deleted\":true}");

            } else {
                sendJson(exchange, 405, JsonUtil.errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendJson(exchange, 500, JsonUtil.errorJson("Server error: " + e.getMessage()));
        }
    }

    // ---------------------- /api/profile/income : POST ----------------------

    private void handleProfileIncome(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            sendJson(exchange, 405, JsonUtil.errorJson("Use POST"));
            return;
        }
        try {
            Map<String, String> body = JsonUtil.parseFlatObject(readBody(exchange));
            double income = Double.parseDouble(body.getOrDefault("monthlyIncome", "0"));
            UserProfile updated = profileManager.updateIncome(income);
            if (updated == null) {
                sendJson(exchange, 404, JsonUtil.errorJson("No profile exists yet"));
                return;
            }
            sendJson(exchange, 200, JsonUtil.profileToJson(updated));
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, JsonUtil.errorJson("monthlyIncome must be a number"));
        }
    }

    // ---------------------- /api/categories : GET (HashMap) ----------------------

    private void handleCategories(HttpExchange exchange) throws IOException {
        Map<String, Double> totals = manager.getCategoryTotals();
        String json = "{\"totals\":" + JsonUtil.mapToJsonObject(totals)
                + ",\"grandTotal\":" + manager.getTotalSpent() + "}";
        sendJson(exchange, 200, json);
    }

    // ---------------------- /api/top : GET (Max-Heap) ----------------------

    private void handleTopExpenses(HttpExchange exchange) throws IOException {
        Map<String, String> params = JsonUtil.parseQuery(exchange.getRequestURI().getQuery());
        int n;
        try {
            n = Integer.parseInt(params.getOrDefault("n", "3"));
        } catch (NumberFormatException e) {
            n = 3;
        }
        List<Expense> top = manager.getTopExpenses(n);
        sendJson(exchange, 200, JsonUtil.expensesToJsonArray(top));
    }

    // ---------------------- /api/activity : GET (Linked List) ----------------------

    private void handleActivity(HttpExchange exchange) throws IOException {
        Map<String, String> params = JsonUtil.parseQuery(exchange.getRequestURI().getQuery());
        int limit;
        try {
            limit = Integer.parseInt(params.getOrDefault("limit", "15"));
        } catch (NumberFormatException e) {
            limit = 15;
        }
        List<String> log = manager.getRecentActivity(limit);
        sendJson(exchange, 200, JsonUtil.stringsToJsonArray(log));
    }

    // ---------------------- /api/reminders : POST (Queue - schedule) ----------------------

    private void handleReminderSchedule(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (!path.equals("/api/reminders") || !exchange.getRequestMethod().equals("POST")) {
            sendJson(exchange, 404, JsonUtil.errorJson("Not found"));
            return;
        }
        Map<String, String> body = JsonUtil.parseFlatObject(readBody(exchange));
        String text = body.getOrDefault("text", "").trim();
        if (text.isEmpty()) {
            sendJson(exchange, 400, JsonUtil.errorJson("Reminder text is required"));
            return;
        }
        manager.scheduleReminder(text);
        sendJson(exchange, 201, "{\"queued\":true}");
    }

    // ---------------------- /api/reminders/process : POST (Queue - dequeue) ----------------------

    private void handleReminderProcess(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            sendJson(exchange, 405, JsonUtil.errorJson("Use POST"));
            return;
        }
        String next = manager.processNextReminder();
        String json = next == null ? "{\"reminder\":null}" : "{\"reminder\":\"" + JsonUtil.escape(next) + "\"}";
        sendJson(exchange, 200, json);
    }

    // ---------------------- /api/reminders/count : GET (Queue - size) ----------------------

    private void handleReminderCount(HttpExchange exchange) throws IOException {
        sendJson(exchange, 200, "{\"count\":" + manager.pendingReminderCount() + "}");
    }

    // ---------------------- static file serving (frontend) ----------------------

    private void handleStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.isEmpty()) path = "/index.html";

        File file = new File(webRoot, path).getCanonicalFile();
        // Basic path-traversal guard: served file must stay inside webRoot.
        if (!file.getPath().startsWith(webRoot.getCanonicalPath())) {
            sendText(exchange, 403, "Forbidden", "text/plain");
            return;
        }
        if (!file.exists() || file.isDirectory()) {
            sendText(exchange, 404, "Not found", "text/plain");
            return;
        }

        String contentType = guessContentType(file.getName());
        byte[] bytes;
        try (InputStream in = new FileInputStream(file)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = in.read(chunk)) != -1) buffer.write(chunk, 0, read);
            bytes = buffer.toByteArray();
        }
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String guessContentType(String filename) {
        if (filename.endsWith(".html")) return "text/html; charset=utf-8";
        if (filename.endsWith(".css")) return "text/css; charset=utf-8";
        if (filename.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (filename.endsWith(".json")) return "application/json; charset=utf-8";
        if (filename.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    // ---------------------- shared helpers ----------------------

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = in.read(chunk)) != -1) buffer.write(chunk, 0, read);
            return buffer.toString(StandardCharsets.UTF_8.name());
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        sendText(exchange, status, json, "application/json; charset=utf-8");
    }

    private void sendText(HttpExchange exchange, int status, String text, String contentType) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
