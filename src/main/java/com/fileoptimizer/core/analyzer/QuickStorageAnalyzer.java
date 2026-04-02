package com.fileoptimizer.core.analyzer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class QuickStorageAnalyzer {

    public Map<String, Long> analyze(Path root) {
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("Images", 0L);
        distribution.put("Videos", 0L);
        distribution.put("Documents", 0L);
        distribution.put("Others", 0L);

        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        String ext = getExtension(file);
                        long size = Files.size(file);

                        if (isImage(ext)) {
                            distribution.merge("Images", size, Long::sum);
                        } else if (isVideo(ext)) {
                            distribution.merge("Videos", size, Long::sum);
                        } else if (isDoc(ext)) {
                            distribution.merge("Documents", size, Long::sum);
                        } else {
                            distribution.merge("Others", size, Long::sum);
                        }

                    } catch (Exception ignored) {}

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    // 👉 bỏ qua file bị từ chối quyền
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (Exception e) {
            System.err.println("Quick analysis error: " + e.getMessage());
        }

        return distribution;
    }

    private String getExtension(Path p) {
        String name = p.getFileName().toString();
        int idx = name.lastIndexOf('.');
        return idx == -1 ? "" : name.substring(idx + 1).toLowerCase();
    }

    private boolean isImage(String ext) {
        return Set.of("jpg", "jpeg", "png", "gif", "webp").contains(ext);
    }

    private boolean isVideo(String ext) {
        return Set.of("mp4", "mkv", "avi", "mov").contains(ext);
    }

    private boolean isDoc(String ext) {
        return Set.of("pdf", "docx", "txt", "xlsx", "pptx").contains(ext);
    }
}