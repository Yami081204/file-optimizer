package com.fileoptimizer.app.controller;

import com.fileoptimizer.app.context.ScanSessionContext;
import com.fileoptimizer.app.service.FileInsightService;
import com.fileoptimizer.app.viewmodel.FileViewModel;
import com.fileoptimizer.app.viewmodel.ScanViewModel;
import com.fileoptimizer.common.model.FileCategory;
import com.fileoptimizer.common.model.FileInsights;
import com.fileoptimizer.common.model.FileMetadata;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScanController {

    @FXML private Button scanButton, cancelButton;
    @FXML private Label statusLabel, progressText, resultsSummaryLabel;
    @FXML private ProgressBar progressBar;

    @FXML private TableView<FileViewModel> fileTable;
    @FXML private TableColumn<FileViewModel, Boolean> selectedColumn;
    @FXML private TableColumn<FileViewModel, String> nameColumn, sizeColumn, typeColumn, dateColumn;

    @FXML private VBox emptyState, processingState, resultsState;

    private final ScanViewModel viewModel = new ScanViewModel();
    private final FileInsightService insightService = new FileInsightService();
    private MainController mainController;

    private final List<FileMetadata> allScannedMetadata = new ArrayList<>();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupBindings();
        showState(emptyState);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setupTableColumns() {
        selectedColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectedColumn));
        selectedColumn.setEditable(true);

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        sizeColumn.setCellValueFactory(data -> data.getValue().sizeProperty());
        typeColumn.setCellValueFactory(data -> data.getValue().typeProperty());
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty());

        fileTable.setItems(viewModel.getFileResults());
        fileTable.setEditable(true);
    }

    private void setupBindings() {
        statusLabel.textProperty().bind(viewModel.statusProperty());
        progressBar.progressProperty().bind(viewModel.progressProperty());

        viewModel.scanningProperty().addListener((obs, oldVal, newVal) -> {
            setScanningUI(newVal);

            if (newVal) {
                showState(processingState);
            } else if (viewModel.statusProperty().get().contains("Completed")) {
                showState(resultsState);
                updateSummary();

                ScanSessionContext.getInstance()
                        .setScannedFiles(new ArrayList<>(allScannedMetadata));

                if (mainController != null && viewModel.lastInsightsProperty().get() != null) {
                    mainController.onScanFinished(viewModel.lastInsightsProperty().get());
                }
            }
        });

        viewModel.totalFilesProperty().addListener((obs, old, val) -> {
            Platform.runLater(() ->
                    progressText.setText(String.format("Found %d items | %s",
                            val.longValue(), formatFileSize(viewModel.totalSizeProperty().get())))
            );
        });
    }

    @FXML
    public void handleScan(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder");
        File dir = chooser.showDialog(scanButton.getScene().getWindow());

        if (dir != null) {
            startScan(dir);
        }
    }

    private void startScan(File directory) {
        Platform.runLater(() -> viewModel.getFileResults().clear());

        viewModel.clearResults();
        allScannedMetadata.clear();
        ScanSessionContext.getInstance().clear();

        viewModel.setScanning(true);
        viewModel.setStatus("Scanning...");
        viewModel.setProgress(-1);

        insightService.scanAndObserve(directory.toPath(), new FileInsightService.ScanObserver() {

            @Override
            public void onFilesFound(List<FileMetadata> files) {
                files.forEach(file -> {
                    if (isJunkFile(file)) {
                        file.setCategory(FileCategory.TEMP);
                        file.setSelected(true);
                    }
                });

                allScannedMetadata.addAll(files);

                Platform.runLater(() -> viewModel.addFileResults(files));
            }

            @Override
            public void onProgress(long totalFiles, long totalSize) {
                Platform.runLater(() -> {
                    viewModel.setTotalFiles(totalFiles);
                    viewModel.setTotalSize(totalSize);
                });
            }

            @Override
            public void onDirectoryChanged(java.nio.file.Path dir) {
                Platform.runLater(() -> viewModel.setStatus("Scanning: " + dir.getFileName()));
            }

            @Override
            public void onCompleted(FileInsights insights) {
                Platform.runLater(() -> {
                    viewModel.setScanning(false);
                    viewModel.setStatus("Scan Completed");
                    viewModel.setProgress(1.0);
                    viewModel.setLastInsights(insights);
                    showState(resultsState);
                });
            }

            @Override
            public void onError(Throwable e) {
                Platform.runLater(() -> {
                    viewModel.setScanning(false);
                    viewModel.setStatus("Error: " + e.getMessage());
                });
            }
        });
    }

    private boolean isJunkFile(FileMetadata file) {
        String name = file.getName() != null ? file.getName().toLowerCase() : "";
        return file.getSize() == 0 || name.endsWith(".tmp") || name.endsWith(".log");
    }

    private void showState(VBox stateToShow) {
        emptyState.setVisible(false);
        processingState.setVisible(false);
        resultsState.setVisible(false);

        emptyState.setManaged(false);
        processingState.setManaged(false);
        resultsState.setManaged(false);

        stateToShow.setVisible(true);
        stateToShow.setManaged(true);
        stateToShow.toFront();
    }

    private void setScanningUI(boolean scanning) {
        scanButton.setDisable(scanning);
        cancelButton.setVisible(scanning);
    }

    private void updateSummary() {
        var insights = viewModel.lastInsightsProperty().get();
        if (insights != null) {
            resultsSummaryLabel.setText(String.format("Found %d items | Total Size: %s",
                    insights.getTotalFiles(), formatFileSize(insights.getTotalSize())));
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        insightService.cancelScan();
        viewModel.setScanning(false);
        viewModel.setStatus("Cancelled");
        showState(emptyState);
    }

    @FXML
    public void handleCleanAll(ActionEvent event) {
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
