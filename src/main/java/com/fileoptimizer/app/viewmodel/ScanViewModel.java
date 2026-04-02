package com.fileoptimizer.app.viewmodel;

import com.fileoptimizer.common.model.FileInsights;
import com.fileoptimizer.common.model.FileMetadata;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

public class ScanViewModel {

    private final StringProperty status = new SimpleStringProperty("Ready");
    private final LongProperty totalFiles = new SimpleLongProperty(0);
    private final LongProperty totalSize = new SimpleLongProperty(0);
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final BooleanProperty scanning = new SimpleBooleanProperty(false);

    private final ObservableList<FileViewModel> fileResults = FXCollections.observableArrayList();
    private final ObjectProperty<FileInsights> lastInsights = new SimpleObjectProperty<>();

    private static final int MAX_FILE_LIST = 1000;

    public void clearResults() {
        fileResults.clear();
        totalFiles.set(0);
        totalSize.set(0);
        progress.set(0);
    }

    public void addFileResults(List<FileMetadata> metadataList) {
        if (metadataList == null || metadataList.isEmpty()) return;

        List<FileViewModel> list = metadataList.stream()
                .map(FileViewModel::new)
                .collect(Collectors.toList());

        fileResults.addAll(list); // 🔥 FIX

        if (fileResults.size() > MAX_FILE_LIST) {
            fileResults.remove(0, fileResults.size() - MAX_FILE_LIST);
        }
    }

    public ObservableList<FileViewModel> getFileResults() {
        return fileResults;
    }

    public StringProperty statusProperty() { return status; }
    public LongProperty totalFilesProperty() { return totalFiles; }
    public LongProperty totalSizeProperty() { return totalSize; }
    public DoubleProperty progressProperty() { return progress; }
    public BooleanProperty scanningProperty() { return scanning; }
    public ObjectProperty<FileInsights> lastInsightsProperty() { return lastInsights; }

    public void setStatus(String s) { status.set(s); }
    public void setTotalFiles(long v) { totalFiles.set(v); }
    public void setTotalSize(long v) { totalSize.set(v); }
    public void setProgress(double v) { progress.set(v); }
    public void setScanning(boolean v) { scanning.set(v); }
    public void setLastInsights(FileInsights i) { lastInsights.set(i); }
}