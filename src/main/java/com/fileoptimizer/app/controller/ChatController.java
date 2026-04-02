package com.fileoptimizer.app.controller;

import com.fileoptimizer.app.service.AiChatService;
import com.fileoptimizer.common.model.FileInsights;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ChatController {

    @FXML private VBox chatHistory;
    @FXML private TextField userInput;
    @FXML private Button clearBtn;

    private MainController mainController;
    private FileInsights insights;

    private final AiChatService aiChatService = new AiChatService();

    @FXML
    public void initialize() {

        appendMessage("AI",
                "Xin chào 👋\n" +
                        "Tôi có thể giúp bạn:\n" +
                        "- Phân tích hệ thống\n" +
                        "- Quét file\n" +
                        "- Dọn rác\n" +
                        "- Tìm file trùng\n" +
                        "- Trả lời câu hỏi thông minh (AI local)\n"
        );

        userInput.setOnAction(e -> handleSendMessage());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setInsights(FileInsights insights) {
        this.insights = insights;
    }

    @FXML
    private void handleSendMessage() {
        String input = userInput.getText().trim();
        if (input.isEmpty()) return;

        appendMessage("Bạn", input);
        userInput.clear();

        String lowerInput = input.toLowerCase();

        // 1. Rule-based trước
        String localResponse = processVietnamese(lowerInput);

        if (!localResponse.equals("__USE_AI__")) {
            appendMessage("AI", localResponse);
            return;
        }

        // 2. Gọi Ollama AI (async)
        new Thread(() -> {
            String aiResponse = aiChatService.askAI(input);

            Platform.runLater(() -> appendMessage("AI", aiResponse));
        }).start();
    }

    // ================= RULE =================

    private String processVietnamese(String input) {

        if (contains(input, "xin chào", "chào", "hello", "hi")) {
            return "Chào bạn 👋 Tôi có thể giúp gì?";
        }

        if (contains(input, "quét", "scan", "kiểm tra file")) {
            Platform.runLater(() -> mainController.showScan());
            return "Đang mở chức năng quét file...";
        }

        if (contains(input, "trùng", "duplicate", "file trùng")) {
            Platform.runLater(() -> mainController.showDuplicates());
            return "Đang mở tìm file trùng...";
        }

        if (contains(input, "dọn", "clean", "xóa rác")) {
            Platform.runLater(() -> mainController.showAutoClean());
            return "Đang mở dọn dẹp hệ thống...";
        }

        if (contains(input, "phân tích", "hệ thống", "analyze")) {

            if (insights == null) {
                return "⚠️ Bạn cần quét hệ thống trước.";
            }

            return buildReport();
        }


        return "__USE_AI__";
    }

    private boolean contains(String input, String... keywords) {
        for (String k : keywords) {
            if (input.contains(k)) return true;
        }
        return false;
    }

    private String buildReport() {

        long totalSize = insights.getTotalSize();
        int totalFiles = (int) insights.getTotalFiles();

        String size = formatFileSize(totalSize);

        String status = totalSize > 10L * 1024 * 1024 * 1024
                ? "⚠️ Hệ thống nặng, nên dọn dẹp."
                : "✅ Hệ thống ổn định.";

        return "📊 PHÂN TÍCH HỆ THỐNG\n\n" +
                "Số file: " + totalFiles + "\n" +
                "Dung lượng: " + size + "\n\n" +
                status;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        double s = (double) size;

        int digitGroups = (int) (Math.log10(s) / Math.log10(1024));

        return String.format("%.2f %s",
                s / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }

    // ================= UI =================

    @FXML
    private void handleClearChat() {
        chatHistory.getChildren().clear();
        appendMessage("AI", "Đã xóa chat. Bạn cần gì?");
    }

    private void appendMessage(String sender, String message) {
        Label msg = new Label(message);
        msg.setWrapText(true);

        msg.getStyleClass().add("chat-message-bubble");

        if ("Bạn".equals(sender)) {
            msg.getStyleClass().add("chat-message-user");
        } else {
            msg.getStyleClass().add("chat-message-ai");
        }

        chatHistory.getChildren().add(msg);
    }
}