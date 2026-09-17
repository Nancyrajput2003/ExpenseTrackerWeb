package model;

/**
 * A single local user profile. This app is a single-user local tool
 * (no real authentication/security) so "login" is just a friendly
 * onboarding form, and "logout" simply returns to the welcome screen
 * without deleting anything.
 */
public class UserProfile {

    private String name;
    private String fullName;
    private String email;
    private double monthlyIncome;

    public UserProfile(String name, String fullName, String email, double monthlyIncome) {
        this.name = name;
        this.fullName = fullName;
        this.email = email;
        this.monthlyIncome = monthlyIncome;
    }

    public String getName() { return name; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public double getMonthlyIncome() { return monthlyIncome; }

    public void setName(String name) { this.name = name; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setMonthlyIncome(double monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    private static String escape(String s) {
        return s == null ? "" : s.replace(",", ";");
    }

    private static String unescape(String s) {
        return s == null ? "" : s.replace(";", ",");
    }

    public String toCsvLine() {
        return escape(name) + "," + escape(fullName) + "," + escape(email) + "," + monthlyIncome;
    }

    public static UserProfile fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        String name = unescape(parts[0]);
        String fullName = unescape(parts[1]);
        String email = unescape(parts[2]);
        double income = parts.length > 3 && !parts[3].isEmpty() ? Double.parseDouble(parts[3]) : 0.0;
        return new UserProfile(name, fullName, email, income);
    }
}
