package com.fileoptimizer.app.viewmodel;

import com.fileoptimizer.common.model.FileMetadata;
import javafx.beans.property.*;

import java.time.format.DateTimeFormatter;

public class FileViewModel {

    private final FileMetadata metadata;
    private final StringProperty name;
    private final StringProperty size;
    private final StringProperty type;
    private final StringProperty date;
    private final BooleanProperty selected;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FileViewModel(FileMetadata metadata) {
        this.metadata = metadata;
        this.name = new SimpleStringProperty(metadata.getName());
        this.size = new SimpleStringProperty(formatFileSize(metadata.getSize()));
        this.type = new SimpleStringProperty(metadata.getCategory() != null ? metadata.getCategory().toString() : "OTHER");
        this.date = new SimpleStringProperty(
                metadata.getModifiedDate() != null ? metadata.getModifiedDate().format(DATE_FORMATTER) : "N/A"
        );
        this.selected = metadata.selectedProperty();
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    // Property Getters
    public StringProperty nameProperty() { return name; }
    public StringProperty sizeProperty() { return size; }
    public StringProperty typeProperty() { return type; }
    public StringProperty dateProperty() { return date; }
    public BooleanProperty selectedProperty() { return selected; }

    public FileMetadata getMetadata() {
        return metadata;
    }
}
