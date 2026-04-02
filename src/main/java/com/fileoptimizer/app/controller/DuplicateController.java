package com.fileoptimizer.app.controller;

import com.fileoptimizer.common.model.FileMetadata;
import com.fileoptimizer.app.service.FileOptimizerFacade;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DuplicateController {

    @FXML private TableView<FileMetadata> duplicateTable;
    @FXML private TableColumn<FileMetadata, Boolean> selectCol;
    @FXML private TableColumn<FileMetadata, String> nameCol;
    @FXML private TableColumn<FileMetadata, String> sizeCol;
    @FXML private TableColumn<FileMetadata, String> pathCol;
    @FXML private TableColumn<FileMetadata, String> hashCol;
    @FXML private Label totalSizeLabel;
    @FXML private Button btnScanDuplicates;

    private final ObservableList<FileMetadata> duplicateList = FXCollections.observableArrayList();
    private final FileOptimizerFacade facade = new FileOptimizerFacade();
    private List<FileMetadata> allScannedFiles = new ArrayList<>();

    @FXML
    public void initialize() {
        setupTableColumns();
        duplicateTable.setItems(duplicateList);
    }


    public void setFilesToAnalyze(List<FileMetadata> files) {
        this.allScannedFiles = files;
    }

    private void setupTableColumns() {
        duplicateTable.setEditable(true);
        
        selectCol.setCellValueFactory(data -> data.getValue().selectedProperty());
        selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));
        
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        sizeColumnFactory();
        pathCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPath().toString()));
        hashCol.setCellValueFactory(data -> new SimpleStringProperty("SHA-256 Content Match"));
    }

    private void sizeColumnFactory() {
        sizeCol.setCellValueFactory(data -> new SimpleStringProperty(formatFileSize(data.getValue().getSize())));
    }

    @FXML
    private void handleScanDuplicates() {
        if (allScannedFiles == null || allScannedFiles.isEmpty()) {
            showAlert("No Files", "Please run a system scan first to populate the file list.");
            return;
        }

        btnScanDuplicates.setDisable(true);
        duplicateList.clear();

        Task<Map<String, List<FileMetadata>>> task = new Task<>() {
            @Override
            protected Map<String, List<FileMetadata>> call() {
                return facade.findDuplicates(allScannedFiles);
            }

            @Override
            protected void succeeded() {
                Map<String, List<FileMetadata>> results = getValue();
                results.forEach((hash, files) -> {
                    Platform.runLater(() -> duplicateList.addAll(files));
                });
                
                updateTotalSize();
                btnScanDuplicates.setDisable(false);
            }

            @Override
            protected void failed() {
                btnScanDuplicates.setDisable(false);
                getException().printStackTrace();
            }
        };

        new Thread(task).start();
    }

    @FXML
    private void handleSelectAll() {
        duplicateList.forEach(f -> f.setSelected(true));
    }

    @FXML
    private void handleDeleteSelected() {
        List<FileMetadata> toDelete = new ArrayList<>();
        duplicateList.forEach(f -> {
            if (f.isSelected()) toDelete.add(f);
        });

        if (toDelete.isEmpty()) return;

        int count = facade.executeCleanup(toDelete);
        duplicateList.removeIf(FileMetadata::isSelected);
        updateTotalSize();
        
        showAlert("Success", "Cleaned up " + count + " duplicate files.");
    }

    private void updateTotalSize() {
        long total = duplicateList.stream().mapToLong(FileMetadata::getSize).sum();
        totalSizeLabel.setText("Total Duplicates: " + formatFileSize(total));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
