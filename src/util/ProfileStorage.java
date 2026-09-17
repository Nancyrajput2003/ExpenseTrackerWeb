package util;

import model.UserProfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Persists the single local user profile to a one-line CSV file,
 * mirroring the style of FileStorage for expenses.
 */
public final class ProfileStorage {

    private ProfileStorage() { }

    public static UserProfile load(File file) {
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line == null || line.trim().isEmpty()) return null;
            return UserProfile.fromCsvLine(line);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    public static void save(File file, UserProfile profile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(profile.toCsvLine());
            writer.newLine();
        } catch (IOException e) {
            // Best-effort; in-memory state is still correct for this session.
        }
    }

    public static void delete(File file) {
        if (file.exists()) file.delete();
    }
}
