package com.fileoptimizer.app.service;

import com.fileoptimizer.common.model.ActionPlan;
import com.fileoptimizer.common.model.FileInsights;
import com.fileoptimizer.common.model.FileMetadata;
import com.fileoptimizer.core.analyzer.FileAnalyzer;
import com.fileoptimizer.core.analyzer.FileAnalyzerImpl;
import com.fileoptimizer.core.deletion.FileDeletionService;
import com.fileoptimizer.core.deletion.SafeFileDeletionService;
import com.fileoptimizer.core.scanner.FileScanner;
import com.fileoptimizer.core.scanner.FileScannerImpl;
import com.fileoptimizer.ai.service.AiSuggestionService;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;


public class AutoCleanService {

    private final FileScanner fileScanner = new FileScannerImpl();
    private final AiSuggestionService aiService = new AiSuggestionService();
    private final FileDeletionService deletionService = new SafeFileDeletionService();

    public Task<Integer> createCleanTask(Path root, Consumer<String> onMessage) {
        return new Task<>() {
            @Override
            protected Integer call() throws Exception {
                // 1. Scan & Analyze (Now combined in the new Scanner)
                updateMessage("Scanning and analyzing files...");
                
                List<FileMetadata> metadataList = Collections.synchronizedList(new ArrayList<>());
                CompletableFuture<Void> scanFuture = new CompletableFuture<>();

                fileScanner.startScan(root, new FileScanner.ScanListener() {
                    @Override
                    public void onFilesFound(List<FileMetadata> files) {
                        metadataList.addAll(files);
                    }

                    @Override
                    public void onDirectoryChanged(Path dir) {
                        // Optional: update status with current directory
                    }

                    @Override
                    public void onProgress(long filesCount, long totalSize) {
                        updateMessage("Found " + filesCount + " files...");
                    }

                    @Override
                    public void onCompleted(FileInsights insights) {
                        scanFuture.complete(null);
                    }

                    @Override
                    public void onError(Throwable t) {
                        scanFuture.completeExceptionally(t);
                    }
                });

                try {
                    scanFuture.get(); // Wait for scan to finish (blocks Task thread, not FX thread)
                } catch (InterruptedException | ExecutionException e) {
                    throw new Exception("Auto Clean failed during scan: " + e.getMessage(), e);
                }

                // 2. Suggest
                updateMessage("Generating AI suggestions...");
                ActionPlan plan = aiService.suggest(new ArrayList<>(metadataList));
                List<FileMetadata> toDelete = plan.getFilesToDelete();
                int totalToDelete = toDelete.size();

                if (totalToDelete == 0) {
                    updateMessage("No files to clean. System is already optimized.");
                    return 0;
                }

                // 3. Delete
                updateMessage("Cleaning up " + totalToDelete + " files...");
                int deletedCount = 0;
                for (int i = 0; i < totalToDelete; i++) {
                    if (isCancelled()) break;
                    
                    FileMetadata file = toDelete.get(i);
                    try {
                        if (deletionService.delete(file)) {
                            deletedCount++;
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to delete " + file.getPath());
                    }
                    updateProgress(i + 1, totalToDelete);
                }

                updateMessage("Auto Clean finished! Removed " + deletedCount + " files.");
                return deletedCount;
            }
        };
    }
}
