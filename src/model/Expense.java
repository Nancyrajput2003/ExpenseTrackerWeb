package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Core data model representing a single expense entry.
 * Implements Comparable so it can be used directly by custom
 * sorting algorithms (sorted by date by default).
 */
public class Expense implements Comparable<Expense> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final int id;
    private String title;
    private double amount;
    private String category;
    private LocalDate date;
    private String note;

    public Expense(int id, String title, double amount, String category, LocalDate date, String note) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
    public String getNote() { return note; }

    public void setTitle(String title) { this.title = title; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setCategory(String category) { this.category = category; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setNote(String note) { this.note = note; }

    @Override
    public int compareTo(Expense other) {
        return this.date.compareTo(other.date);
    }

    public String getFormattedDate() {
        return date.format(FMT);
    }

    public String toCsvLine() {
        return id + "," + escape(title) + "," + amount + "," + escape(category) + ","
                + date.format(FMT) + "," + escape(note == null ? "" : note);
    }

    public static Expense fromCsvLine(String line) {
        String[] parts = splitCsv(line);
        int id = Integer.parseInt(parts[0]);
        String title = unescape(parts[1]);
        double amount = Double.parseDouble(parts[2]);
        String category = unescape(parts[3]);
        LocalDate date = LocalDate.parse(parts[4], FMT);
        String note = parts.length > 5 ? unescape(parts[5]) : "";
        return new Expense(id, title, amount, category, date, note);
    }

    private static String escape(String s) {
        return s.replace(",", ";");
    }

    private static String unescape(String s) {
        return s.replace(";", ",");
    }

    private static String[] splitCsv(String line) {
        return line.split(",", -1);
    }

    @Override
    public String toString() {
        return String.format("#%-4d %-10s %-18s Rs.%-10.2f %-12s %s",
                id, getFormattedDate(), title, amount, category, note == null ? "" : note);
    }
}
