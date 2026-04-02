package com.fileoptimizer.core.scanner;

import com.fileoptimizer.common.model.FileCategory;
import com.fileoptimizer.common.model.FileInsights;
import com.fileoptimizer.common.model.FileMetadata;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class FileScannerImpl implements FileScanner {

    private final ForkJoinPool forkJoinPool;
    private final AtomicBoolean isScanning = new AtomicBoolean(false);
    private final AtomicBoolean abortSignal = new AtomicBoolean(false);
    private final AtomicLong totalFiles = new AtomicLong(0);
    private final AtomicLong totalSize = new AtomicLong(0);

    public FileScannerImpl() {
        // Use a pool size based on available processors, but capped to avoid excessive overhead
        this.forkJoinPool = new ForkJoinPool(Math.min(Runtime.getRuntime().availableProcessors(), 8));
    }

    @Override
    public void startScan(Path root, ScanListener listener) {
        if (isScanning.getAndSet(true)) {
            listener.onError(new IllegalStateException("A scan is already in progress."));
            return;
        }

        abortSignal.set(false);
        totalFiles.set(0);
        totalSize.set(0);
        FileInsights insights = new FileInsights();

        // Use a separate thread to launch the ForkJoin task to avoid blocking the caller
        CompletableFuture.runAsync(() -> {
            try {
                // Heartbeat to UI that scan has started
                Platform.runLater(() -> listener.onDirectoryChanged(root));

                ScanTask rootTask = new ScanTask(root, listener, insights, abortSignal);
                forkJoinPool.invoke(rootTask);
                
                if (!abortSignal.get()) {
                    Platform.runLater(() -> {
                        // Ensure final progress is accurate
                        listener.onProgress(totalFiles.get(), totalSize.get());
                        listener.onCompleted(insights);
                    });
                }
            } catch (Exception e) {
                if (!abortSignal.get()) {
                    Platform.runLater(() -> listener.onError(e));
                }
            } finally {
                isScanning.set(false);
            }
        });
    }

    @Override
    public void cancelScan() {
        abortSignal.set(true);
        isScanning.set(false);
    }

    @Override
    public boolean isScanning() {
        return isScanning.get();
    }

    private class ScanTask extends RecursiveAction {
        private final Path dir;
        private final ScanListener listener;
        private final FileInsights insights;
        private final AtomicBoolean abortSignal;
        
        // Local buffer for batching updates within this task
        private final List<FileMetadata> localBuffer = new ArrayList<>();
        private static final int BATCH_SIZE = 100;

        ScanTask(Path dir, ScanListener listener, FileInsights insights, AtomicBoolean abortSignal) {
            this.dir = dir;
            this.listener = listener;
            this.insights = insights;
            this.abortSignal = abortSignal;
        }

        @Override
        protected void compute() {
            if (abortSignal.get()) return;

            if (dir.getNameCount() < 4 || ThreadLocalRandom.current().nextInt(100) < 5) {
                Platform.runLater(() -> listener.onDirectoryChanged(dir));
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                List<ScanTask> subTasks = new ArrayList<>();

                for (Path path : stream) {
                    if (abortSignal.get()) break;

                    if (Files.isDirectory(path)) {
                        if (!isExcluded(path)) {
                            ScanTask task = new ScanTask(path, listener, insights, abortSignal);
                            task.fork();
                            subTasks.add(task);
                        }
                    } else {
                        processFile(path);
                    }
                }

                flushBuffer();

                for (ScanTask task : subTasks) {
                    if (abortSignal.get()) break;
                    task.join();
                }

            } catch (IOException | DirectoryIteratorException ignored) {
                // Silently skip locked folders or access denied errors
            }
        }

        private void processFile(Path path) {
            if (abortSignal.get()) return;

            try {
                BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                if (attrs.isRegularFile()) {
                    FileMetadata metadata = createMetadata(path, attrs);
                    
                    synchronized (insights) {
                        insights.addFile(metadata);
                    }

                    localBuffer.add(metadata);
                    
                    long count = totalFiles.incrementAndGet();
                    long size = totalSize.addAndGet(attrs.size());

                    if (localBuffer.size() >= BATCH_SIZE) {
                        flushBuffer();
                    }

                    // Throttled progress updates
                    if (count % 100 == 0) {
                        Platform.runLater(() -> listener.onProgress(count, size));
                    }
                }
            } catch (IOException ignored) {}
        }

        private void flushBuffer() {
            if (!localBuffer.isEmpty()) {
                List<FileMetadata> batch = new ArrayList<>(localBuffer);
                localBuffer.clear();
                Platform.runLater(() -> listener.onFilesFound(batch));
            }
        }

        private FileMetadata createMetadata(Path path, BasicFileAttributes attrs) {
            String name = path.getFileName().toString();
            String ext = getExtension(name);
            FileCategory category = determineCategory(ext);

            FileMetadata meta = new FileMetadata();
            meta.setPath(path);
            meta.setName(name);
            meta.setExtension(ext);
            meta.setSize(attrs.size());
            meta.setModifiedDate(LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault()));
            meta.setCreatedDate(LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault()));
            meta.setCategory(category);
            
            return meta;
        }

        private boolean isExcluded(Path path) {
            String name = path.getFileName().toString().toLowerCase();
            return name.startsWith(".") 
                || name.contains("system volume information")
                || name.equals("$recycle.bin")
                || name.equals("windows")
                || name.equals("program files")
                || name.equals("program files (x86)")
                || name.equals("appdata");
        }

        private String getExtension(String fileName) {
            int dotIndex = fileName.lastIndexOf('.');
            return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
        }

        private FileCategory determineCategory(String ext) {
            return switch (ext) {
                case "jpg", "jpeg", "png", "gif", "bmp", "webp" -> FileCategory.IMAGE;
                case "mp4", "mkv", "avi", "mov", "wmv", "flv" -> FileCategory.VIDEO;
                case "pdf", "doc", "docx", "txt", "rtf", "odt", "xls", "xlsx", "ppt", "pptx" -> FileCategory.DOCUMENT;
                case "mp3", "wav", "flac", "aac", "ogg", "m4a" -> FileCategory.AUDIO;
                case "zip", "rar", "7z", "tar", "gz" -> FileCategory.ARCHIVE;
                case "tmp", "temp", "bak" -> FileCategory.TEMP;
                default -> FileCategory.OTHER;
            };
        }
    }
}
