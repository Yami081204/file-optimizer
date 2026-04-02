package com.fileoptimizer.data.entity;

import com.fileoptimizer.common.model.FileCategory;
import java.time.LocalDateTime;

public class FileActionHistory {
    private String extension;
    private FileCategory category;
    private int deleteCount;
    private int keepCount;
    private LocalDateTime lastActionDate;

    public FileActionHistory(String extension, FileCategory category) {
        this.extension = extension;
        this.category = category;
        this.deleteCount = 0;
        this.keepCount = 0;
        this.lastActionDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getExtension() { return extension; }
    public FileCategory getCategory() { return category; }
    public int getDeleteCount() { return deleteCount; }
    public void incrementDelete() { this.deleteCount++; this.lastActionDate = LocalDateTime.now(); }
    public int getKeepCount() { return keepCount; }
    public void incrementKeep() { this.keepCount++; this.lastActionDate = LocalDateTime.now(); }
    public LocalDateTime getLastActionDate() { return lastActionDate; }
}
