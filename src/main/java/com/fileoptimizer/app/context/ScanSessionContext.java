package com.fileoptimizer.app.context;

import com.fileoptimizer.common.model.FileMetadata;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ScanSessionContext {
    private static ScanSessionContext instance;

    private final ObservableList<FileMetadata> scannedFiles = FXCollections.observableArrayList();
    private Map<String, List<FileMetadata>> duplicateGroups;

    private ScanSessionContext() {}

    public static synchronized ScanSessionContext getInstance() {
        if (instance == null) {
            instance = new ScanSessionContext();
        }
        return instance;
    }

    public ObservableList<FileMetadata> getScannedFiles() {
        return scannedFiles;
    }

    public void setScannedFiles(List<FileMetadata> files) {
        this.scannedFiles.setAll(files);
    }

    public void addScannedFiles(List<FileMetadata> files) {
        this.scannedFiles.addAll(files);
    }

    public void clear() {
        scannedFiles.clear();
        duplicateGroups = null;
    }

    public Map<String, List<FileMetadata>> getDuplicateGroups() {
        return duplicateGroups;
    }

    public void setDuplicateGroups(Map<String, List<FileMetadata>> duplicateGroups) {
        this.duplicateGroups = duplicateGroups;
    }
}
