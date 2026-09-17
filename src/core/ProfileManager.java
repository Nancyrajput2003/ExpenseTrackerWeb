package core;

import model.UserProfile;
import util.ProfileStorage;

import java.io.File;

/**
 * Manages the single local user profile: onboarding, income updates,
 * logout (no-op server-side, handled client-side), and account deletion.
 */
public class ProfileManager {

    private final File profileFile;
    private UserProfile profile;

    public ProfileManager(File profileFile) {
        this.profileFile = profileFile;
        this.profile = ProfileStorage.load(profileFile);
    }

    public boolean exists() {
        return profile != null;
    }

    public UserProfile get() {
        return profile;
    }

    public UserProfile createOrUpdate(String name, String fullName, String email) {
        double existingIncome = profile != null ? profile.getMonthlyIncome() : 0.0;
        profile = new UserProfile(name, fullName, email, existingIncome);
        ProfileStorage.save(profileFile, profile);
        return profile;
    }

    public UserProfile updateIncome(double monthlyIncome) {
        if (profile == null) return null;
        profile.setMonthlyIncome(monthlyIncome);
        ProfileStorage.save(profileFile, profile);
        return profile;
    }

    public void deleteAccount() {
        profile = null;
        ProfileStorage.delete(profileFile);
    }
}
