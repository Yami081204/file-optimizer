package com.fileoptimizer.core.analyzer;

import com.fileoptimizer.common.model.FileCategory;
import com.fileoptimizer.common.model.FileInsights;
import com.fileoptimizer.common.model.FileMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Map;

public class FileAnalyzerImpl implements FileAnalyzer {

    private static final Map<String, FileCategory> CATEGORY_MAP = Map.ofEntries(
            Map.entry("jpg", FileCategory.IMAGE),
            Map.entry("jpeg", FileCategory.IMAGE),
            Map.entry("png", FileCategory.IMAGE),
            Map.entry("gif", FileCategory.IMAGE),
            Map.entry("webp", FileCategory.IMAGE),
            Map.entry("mp4", FileCategory.VIDEO),
            Map.entry("mkv", FileCategory.VIDEO),
            Map.entry("avi", FileCategory.VIDEO),
            Map.entry("mov", FileCategory.VIDEO),
            Map.entry("mp3", FileCategory.AUDIO),
            Map.entry("wav", FileCategory.AUDIO),
            Map.entry("pdf", FileCategory.DOCUMENT),
            Map.entry("docx", FileCategory.DOCUMENT),
            Map.entry("doc", FileCategory.DOCUMENT),
            Map.entry("txt", FileCategory.DOCUMENT),
            Map.entry("xlsx", FileCategory.DOCUMENT),
            Map.entry("pptx", FileCategory.DOCUMENT),
            Map.entry("zip", FileCategory.ARCHIVE),
            Map.entry("rar", FileCategory.ARCHIVE),
            Map.entry("7z", FileCategory.ARCHIVE),
            Map.entry("tmp", FileCategory.TEMP)
    );

    @Override
    public FileMetadata analyze(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("The provided path is not a file: " + path);
        }

        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        String name = path.getFileName().toString();
        String extension = getFileExtension(name);
        FileCategory category = categorize(extension);

        LocalDateTime createdDate = LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault());
        LocalDateTime modifiedDate = LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());

        FileMetadata metadata = new FileMetadata();
        metadata.setPath(path);
        metadata.setName(name);
        metadata.setExtension(extension);
        metadata.setSize(attrs.size());
        metadata.setCreatedDate(createdDate);
        metadata.setModifiedDate(modifiedDate);
        metadata.setCategory(category);
        metadata.setScore(0.0);

        return metadata;
    }

    public FileInsights generateInsights(Collection<FileMetadata> files) {
        FileInsights insights = new FileInsights();
        files.forEach(insights::addFile);
        return insights;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }

    private FileCategory categorize(String extension) {
        return CATEGORY_MAP.getOrDefault(extension, FileCategory.OTHER);
    }
}
