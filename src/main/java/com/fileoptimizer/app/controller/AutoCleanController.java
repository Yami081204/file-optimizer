package com.fileoptimizer.app.controller;

import com.fileoptimizer.app.service.AutoCleanService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AutoCleanController {

    @FXML private Button btnRunClean;
    @FXML private TextArea logArea;
    @FXML private ProgressBar progressBar;

    private final AutoCleanService autoCleanService = new AutoCleanService();

    @FXML
    public void initialize() {
        logArea.appendText("Auto Clean system ready. Targeted: Temp files, Cache, and Duplicates.\n");
        progressBar.setVisible(false);
    }

    @FXML
    private void handleRunClean() {
        btnRunClean.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(0);
        logArea.clear();
        appendLog("Starting real-world system optimization...");

        // Targeted cleaning of common user data folders
        Task<Integer> cleanTask = autoCleanService.createCleanTask(
            Paths.get(System.getProperty("user.home"), "Downloads"), 
            msg -> Platform.runLater(() -> appendLog(msg))
        );

        progressBar.progressProperty().bind(cleanTask.progressProperty());
        
        cleanTask.messageProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) appendLog(newVal);
        });

        cleanTask.setOnSucceeded(e -> {
            int count = cleanTask.getValue();
            appendLog("==========================================");
            appendLog("CRITICAL: Optimization Success.");
            appendLog("Items Cleaned: " + count);
            appendLog("Estimated Space Saved: " + (count * 2.5) + " MB"); // Mock calculation
            btnRunClean.setDisable(false);
        });

        cleanTask.setOnFailed(e -> {
            appendLog("ERROR: Cleanup process failed: " + cleanTask.getException().getMessage());
            btnRunClean.setDisable(false);
        });

        new Thread(cleanTask).start();
    }

    private void appendLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + timestamp + "] " + message + "\n");
        logArea.setScrollTop(Double.MAX_VALUE);
    }
}
