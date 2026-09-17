import core.ExpenseManager;
import core.ProfileManager;
import web.ApiServer;

import java.io.File;

/**
 * Starts the Expense Tracker web app.
 * Run with: java -cp bin Main [port]
 * Then open http://localhost:8080 (or the port you passed) in a browser.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        int port;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                port = Integer.parseInt(
                        System.getenv().getOrDefault("PORT", "8080")
                );
            }
        } else {
            port = Integer.parseInt(
                    System.getenv().getOrDefault("PORT", "8080")
            );
        }

        File dataFile = new File("expenses.csv");
        File profileFile = new File("profile.csv");
        File webRoot = new File("webroot");

        if (!webRoot.exists()) {
            System.out.println("WARNING: 'webroot' folder not found at " + webRoot.getAbsolutePath());
            System.out.println("Run this from the ExpenseTrackerWeb project root so 'webroot/' resolves.");
        }

        ExpenseManager manager = new ExpenseManager(dataFile);
        ProfileManager profileManager = new ProfileManager(profileFile);
        ApiServer server = new ApiServer(manager, profileManager, webRoot);
        server.start(port);

        System.out.println("Expense Tracker running at http://localhost:" + port);
        System.out.println("Data persisted to: " + dataFile.getAbsolutePath());
        System.out.println("Press Ctrl+C to stop.");
    }
}
