package util;

import model.Expense;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple CSV-based persistence so data survives between app launches.
 * Uses plain java.io (not java.nio.file) so it works safely on Android's
 * lower API levels, storing the file inside the app's private data
 * directory (context.getFilesDir()).
 */
public final class FileStorage {

    private FileStorage() { }

    public static List<Expense> load(File file) {
        List<Expense> expenses = new ArrayList<>();
        if (!file.exists()) return expenses;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                expenses.add(Expense.fromCsvLine(line));
            }
        } catch (IOException e) {
            // Starting fresh if the file can't be read is an acceptable fallback.
        }
        return expenses;
    }

    public static void save(File file, List<Expense> expenses) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Expense e : expenses) {
                writer.write(e.toCsvLine());
                writer.newLine();
            }
        } catch (IOException e) {
            // Silently ignore; the in-memory state is still correct for this session.
        }
    }
}
