package com.fileoptimizer.app.service;

import java.io.File;
import java.util.prefs.Preferences;

public class SettingsService {

    private final Preferences prefs = Preferences.userNodeForPackage(SettingsService.class);

    public boolean isIncludeHiddenFiles() { return prefs.getBoolean("includeHidden", false); }
    public void setIncludeHiddenFiles(boolean value) { prefs.putBoolean("includeHidden", value); }

    public boolean isScanSystemDirs() { return prefs.getBoolean("scanSystemDirs", false); }
    public void setScanSystemDirs(boolean value) { prefs.putBoolean("scanSystemDirs", value); }

    public boolean isScanLargeFilesOnly() { return prefs.getBoolean("scanLargeOnly", false); }
    public void setScanLargeFilesOnly(boolean value) { prefs.putBoolean("scanLargeOnly", value); }

    public String getScanPath() {
        return prefs.get("scanPath", System.getProperty("user.home"));
    }

    public void setScanPath(String path) {
        prefs.put("scanPath", path);
    }

    public boolean isAutoCleanEnabled() { return prefs.getBoolean("autoCleanEnabled", false); }
    public void setAutoCleanEnabled(boolean value) { prefs.putBoolean("autoCleanEnabled", value); }

    public String getAutoCleanFrequency() { return prefs.get("autoCleanFreq", "Weekly"); }
    public void setAutoCleanFrequency(String value) { prefs.put("autoCleanFreq", value); }

    public boolean isDeleteTempFiles() { return prefs.getBoolean("delTemp", true); }
    public void setDeleteTempFiles(boolean value) { prefs.putBoolean("delTemp", value); }

    public boolean isDeleteDuplicates() { return prefs.getBoolean("delDupes", false); }
    public void setDeleteDuplicates(boolean value) { prefs.putBoolean("delDupes", value); }

    public boolean isClearCache() { return prefs.getBoolean("clearCache", true); }
    public void setClearCache(boolean value) { prefs.putBoolean("clearCache", value); }

    public boolean isSafetyValidationEnabled() { return prefs.getBoolean("safetyEnabled", true); }
    public void setSafetyValidationEnabled(boolean value) { prefs.putBoolean("safetyEnabled", value); }

    public boolean isMoveToTrash() { return prefs.getBoolean("moveToTrash", true); }
    public void setMoveToTrash(boolean value) { prefs.putBoolean("moveToTrash", value); }

    public boolean isConfirmBeforeDelete() { return prefs.getBoolean("confirmDelete", true); }
    public void setConfirmBeforeDelete(boolean value) { prefs.putBoolean("confirmDelete", value); }

    public boolean isAiSuggestionsEnabled() { return prefs.getBoolean("aiSuggestions", true); }
    public void setAiSuggestionsEnabled(boolean value) { prefs.putBoolean("aiSuggestions", value); }

    public boolean isAiAutoCleanEnabled() { return prefs.getBoolean("aiAutoClean", false); }
    public void setAiAutoCleanEnabled(boolean value) { prefs.putBoolean("aiAutoClean", value); }

    public double getAiAggressiveness() { return prefs.getDouble("aiAggressiveness", 0.5); }
    public void setAiAggressiveness(double value) { prefs.putDouble("aiAggressiveness", value); }

    public boolean isDarkMode() { return prefs.getBoolean("darkMode", false); }
    public void setDarkMode(boolean value) { prefs.putBoolean("darkMode", value); }

    public void resetAll() {
        try {
            prefs.clear();
        } catch (Exception ignored) {}
    }
}