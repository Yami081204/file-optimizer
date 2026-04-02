package com.fileoptimizer.common.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Represents the metadata of a file within the system.
 * Uses JavaFX properties for UI binding where necessary.
 */
public class FileMetadata {
    private Path path;
    private String name;
    private String extension;
    private long size;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private FileCategory category;
    private double score;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public FileMetadata() {}

    public FileMetadata(Path path, String name, String extension, long size, 
                        LocalDateTime createdDate, LocalDateTime modifiedDate, 
                        FileCategory category, double score) {
        this.path = path;
        this.name = name;
        this.extension = extension;
        this.size = size;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.category = category;
        this.score = score;
    }

    // Getters and Setters

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public FileCategory getCategory() {
        return category;
    }

    public void setCategory(FileCategory category) {
        this.category = category;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    // JavaFX property methods
    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "path=" + path +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", category=" + category +
                '}';
    }
}
