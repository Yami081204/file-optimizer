package com.fileoptimizer.app.controller;

import com.fileoptimizer.common.model.FileInsights;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label viewTitle;
    @FXML private Button btnDashboard, btnScan, btnDuplicates, btnChat, btnAutoClean;

    private final Map<String, Node> viewCache = new HashMap<>();
    private final Map<String, Object> controllerCache = new HashMap<>();
    private Button currentActiveButton;

    // 👉 GLOBAL DATA (AI sẽ dùng)
    private FileInsights latestInsights;

    @FXML
    public void initialize() {
        showDashboard();
    }


    public void showDashboard() {
        loadView("/fxml/dashboard.fxml", "System Dashboard", btnDashboard);

        if (latestInsights != null) {
            DashboardController dash = (DashboardController) controllerCache.get("/fxml/dashboard.fxml");
            if (dash != null) dash.updateInsights(latestInsights);
        }
    }

    public void showScan() {
        loadView("/fxml/scan.fxml", "Scan Engine", btnScan);
    }

    public void showDuplicates() {
        loadView("/fxml/duplicate.fxml", "Duplicate Finder", btnDuplicates);
    }

    public void showChat() {
        loadView("/fxml/chat.fxml", "AI Assistant", btnChat);

        // 👉 Inject dữ liệu vào AI
        ChatController chat = (ChatController) controllerCache.get("/fxml/chat.fxml");
        if (chat != null) {
            chat.setInsights(latestInsights);
        }
    }

    public void showAutoClean() {
        loadView("/fxml/autoclean.fxml", "Auto Cleanup", btnAutoClean);
    }


    @FXML
    private void handleAutoClean() {
        showAutoClean();
    }

    @FXML
    private void handleQuickScan() {
        showScan();
    }

    @FXML
    private void handleExportReport() {
        showInfo("Export", "Report export feature will be available in the next update.");
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("File Optimizer");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }


    public FileInsights getLatestInsights() {
        return latestInsights;
    }

    public void onScanFinished(FileInsights insights) {
        this.latestInsights = insights;

        // Update Dashboard
        DashboardController dash = (DashboardController) controllerCache.get("/fxml/dashboard.fxml");
        if (dash != null) dash.updateInsights(insights);

        // 👉 Update AI luôn
        ChatController chat = (ChatController) controllerCache.get("/fxml/chat.fxml");
        if (chat != null) {
            chat.setInsights(insights);
        }
    }


    private void loadView(String fxmlPath, String title, Button sourceButton) {
        try {
            updateSidebarStyle(sourceButton);
            viewTitle.setText(title);

            Node view = viewCache.get(fxmlPath);

            if (view == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                view = loader.load();

                Object controller = loader.getController();
                injectMainController(controller);

                viewCache.put(fxmlPath, view);
                controllerCache.put(fxmlPath, controller);
            }

            view.setOpacity(0);
            contentArea.getChildren().setAll(view);

            FadeTransition ft = new FadeTransition(Duration.millis(300), view);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void injectMainController(Object controller) {
        if (controller instanceof DashboardController) {
            ((DashboardController) controller).setMainController(this);
        }
        else if (controller instanceof ScanController) {
            ((ScanController) controller).setMainController(this);
        }
        else if (controller instanceof ChatController) {
            ((ChatController) controller).setMainController(this);
        }
    }


    private void updateSidebarStyle(Button activeBtn) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-button-active");
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("nav-button-active");
            currentActiveButton = activeBtn;
        }
    }
}
