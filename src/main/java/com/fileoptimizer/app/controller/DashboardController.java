package com.fileoptimizer.app.controller;

import com.fileoptimizer.common.model.FileCategory;
import com.fileoptimizer.common.model.FileInsights;
import com.fileoptimizer.core.analyzer.QuickStorageAnalyzer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DashboardController {

    @FXML private Label totalFilesLabel, totalSizeLabel;
    @FXML private PieChart sizeDistributionChart;
    @FXML private TableView<Object> activityTable;

    private MainController mainController;

    @FXML
    public void initialize() {

        sizeDistributionChart.setScaleX(1.3);
        sizeDistributionChart.setScaleY(1.3);

        loadDiskOverview();

        runQuickAnalysis();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleScanNow() {
        if (mainController != null) {
            mainController.showScan();
        }
    }

    private void loadDiskOverview() {
        File root = new File("C:\\");

        long total = root.getTotalSpace();
        long free = root.getFreeSpace();
        long used = total - free;

        Map<FileCategory, Long> map = new HashMap<>();
        map.put(FileCategory.OTHER, used);
        map.put(FileCategory.FREE, free);

        FileInsights insights = new FileInsights();
        insights.setTotalSize(used);
        insights.setTotalFiles(0);
        insights.setSizeByCategory(map);

        updateInsights(insights);
    }

    private void runQuickAnalysis() {

        Task<FileInsights> task = new Task<>() {
            @Override
            protected FileInsights call() {
                try {
                    Path root = Paths.get("C:\\");
                    QuickStorageAnalyzer analyzer = new QuickStorageAnalyzer();
                    Map<String, Long> result = analyzer.analyze(root);
                    return convertToInsights(result, root);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

        task.setOnSucceeded(e -> updateInsights(task.getValue()));
        new Thread(task).start();
    }

    private FileInsights convertToInsights(Map<String, Long> map, Path root) {

        FileInsights insights = new FileInsights();

        long totalSize = map.values().stream().mapToLong(Long::longValue).sum();

        File rootFile = root.toFile();
        long totalDisk = rootFile.getTotalSpace();
        long freeSpace = totalDisk - totalSize;

        Map<FileCategory, Long> categoryMap = new HashMap<>();
        categoryMap.put(FileCategory.IMAGE, map.getOrDefault("Images", 0L));
        categoryMap.put(FileCategory.VIDEO, map.getOrDefault("Videos", 0L));
        categoryMap.put(FileCategory.DOCUMENT, map.getOrDefault("Documents", 0L));
        categoryMap.put(FileCategory.OTHER, map.getOrDefault("Others", 0L));
        categoryMap.put(FileCategory.FREE, freeSpace);

        insights.setTotalSize(totalSize);
        insights.setTotalFiles(0);
        insights.setSizeByCategory(categoryMap);

        return insights;
    }

    public void updateInsights(FileInsights insights) {
        if (insights == null) return;

        Platform.runLater(() -> {
            totalFilesLabel.setText(String.format("%,d", insights.getTotalFiles()));
            totalSizeLabel.setText(formatFileSize(insights.getTotalSize()));
            updatePieChart(insights.getSizeByCategory());
        });
    }

    private void updatePieChart(Map<FileCategory, Long> rawMap) {

        Map<String, Long> grouped = new HashMap<>();
        grouped.put("Images", rawMap.getOrDefault(FileCategory.IMAGE, 0L));
        grouped.put("Videos", rawMap.getOrDefault(FileCategory.VIDEO, 0L));
        grouped.put("Documents", rawMap.getOrDefault(FileCategory.DOCUMENT, 0L));
        grouped.put("Others", rawMap.getOrDefault(FileCategory.OTHER, 0L));
        grouped.put("Free", rawMap.getOrDefault(FileCategory.FREE, 0L));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        grouped.forEach((k, v) -> {
            if (v > 0) {
                pieData.add(new PieChart.Data(k, v));
            }
        });

        sizeDistributionChart.setData(pieData);

        double total = grouped.values().stream().mapToLong(Long::longValue).sum();

        Platform.runLater(() -> {
            for (PieChart.Data data : pieData) {

                double percent = (data.getPieValue() / total) * 100;

                data.nameProperty().bind(
                        Bindings.concat(data.getName(), " ", String.format("%.1f%%", percent))
                );

                Node node = data.getNode();
                if (node != null) {
                    Tooltip.install(node,
                            new Tooltip(formatFileSize((long) data.getPieValue()))
                    );
                }
            }
        });
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return String.format("%.2f %s",
                size / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }
}