package com.fileoptimizer.app.controller;

import com.fileoptimizer.app.service.SettingsService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class SettingsController {

    @FXML private CheckBox includeHiddenFiles;
    @FXML private CheckBox scanSystemDirs;
    @FXML private CheckBox scanLargeFilesOnly;

    @FXML private ToggleButton autoCleanEnabled;
    @FXML private ComboBox<String> autoCleanFrequency;
    @FXML private CheckBox deleteTempFiles;
    @FXML private CheckBox deleteDuplicates;
    @FXML private CheckBox clearCache;

    @FXML private CheckBox safetyValidationEnabled;
    @FXML private CheckBox moveToTrash;
    @FXML private CheckBox confirmBeforeDelete;

    @FXML private ToggleButton aiSuggestionsEnabled;
    @FXML private ToggleButton aiAutoCleanEnabled;
    @FXML private Slider aiAggressiveness;

    @FXML private Label cacheSizeLabel;

    // 👉 NEW
    @FXML private Label scanPathLabel;
    @FXML private ToggleButton darkModeToggle;

    private final SettingsService settingsService = new SettingsService();

    @FXML
    public void initialize() {
        loadSettings();
        setupListeners();
        updateCacheSize();
    }

    private void loadSettings() {

        includeHiddenFiles.setSelected(settingsService.isIncludeHiddenFiles());
        scanSystemDirs.setSelected(settingsService.isScanSystemDirs());
        scanLargeFilesOnly.setSelected(settingsService.isScanLargeFilesOnly());

        autoCleanEnabled.setSelected(settingsService.isAutoCleanEnabled());
        autoCleanFrequency.getItems().addAll("Daily", "Weekly", "Monthly");
        autoCleanFrequency.setValue(settingsService.getAutoCleanFrequency());

        deleteTempFiles.setSelected(settingsService.isDeleteTempFiles());
        deleteDuplicates.setSelected(settingsService.isDeleteDuplicates());
        clearCache.setSelected(settingsService.isClearCache());

        safetyValidationEnabled.setSelected(settingsService.isSafetyValidationEnabled());
        moveToTrash.setSelected(settingsService.isMoveToTrash());
        confirmBeforeDelete.setSelected(settingsService.isConfirmBeforeDelete());

        aiSuggestionsEnabled.setSelected(settingsService.isAiSuggestionsEnabled());
        aiAutoCleanEnabled.setSelected(settingsService.isAiAutoCleanEnabled());
        aiAggressiveness.setValue(settingsService.getAiAggressiveness());

        scanPathLabel.setText(settingsService.getScanPath());
        darkModeToggle.setSelected(settingsService.isDarkMode());
    }

    private void setupListeners() {

        includeHiddenFiles.selectedProperty().addListener((o, a, v) -> settingsService.setIncludeHiddenFiles(v));
        scanSystemDirs.selectedProperty().addListener((o, a, v) -> settingsService.setScanSystemDirs(v));
        scanLargeFilesOnly.selectedProperty().addListener((o, a, v) -> settingsService.setScanLargeFilesOnly(v));

        autoCleanEnabled.selectedProperty().addListener((o, a, v) -> settingsService.setAutoCleanEnabled(v));
        autoCleanFrequency.valueProperty().addListener((o, a, v) -> settingsService.setAutoCleanFrequency(v));

        deleteTempFiles.selectedProperty().addListener((o, a, v) -> settingsService.setDeleteTempFiles(v));
        deleteDuplicates.selectedProperty().addListener((o, a, v) -> settingsService.setDeleteDuplicates(v));
        clearCache.selectedProperty().addListener((o, a, v) -> settingsService.setClearCache(v));

        safetyValidationEnabled.selectedProperty().addListener((o, a, v) -> settingsService.setSafetyValidationEnabled(v));
        moveToTrash.selectedProperty().addListener((o, a, v) -> settingsService.setMoveToTrash(v));
        confirmBeforeDelete.selectedProperty().addListener((o, a, v) -> settingsService.setConfirmBeforeDelete(v));

        aiSuggestionsEnabled.selectedProperty().addListener((o, a, v) -> settingsService.setAiSuggestionsEnabled(v));
        aiAutoCleanEnabled.selectedProperty().addListener((o, a, v) -> settingsService.setAiAutoCleanEnabled(v));

        aiAggressiveness.valueProperty().addListener((o, a, v) -> {
            settingsService.setAiAggressiveness(v.doubleValue());
        });

        darkModeToggle.selectedProperty().addListener((o, a, v) -> {
            settingsService.setDarkMode(v);
            applyTheme(v);
        });
    }


    @FXML
    private void handleChooseFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(new Stage());

        if (file != null) {
            settingsService.setScanPath(file.getAbsolutePath());
            scanPathLabel.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleResetSettings() {
        settingsService.resetAll();
        loadSettings();
    }

    @FXML
    private void handleClearAppCache() {
        cacheSizeLabel.setText("Cache size: 0 MB");
    }

    private void updateCacheSize() {
        cacheSizeLabel.setText("Cache size: " + (Math.random() * 50) + " MB");
    }

    private void applyTheme(boolean dark) {
        System.out.println("Switch theme: " + (dark ? "Dark" : "Light"));
    }
}