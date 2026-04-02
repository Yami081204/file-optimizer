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
import com.fileoptimizer.core.duplicate.DuplicateDetector;
import com.fileoptimizer.core.duplicate.HashDuplicateDetector;
import com.fileoptimizer.ai.service.AiSuggestionService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FileOptimizerFacade {

    private final FileScanner fileScanner = new FileScannerImpl();
    private final FileAnalyzer fileAnalyzer = new FileAnalyzerImpl();
    private final FileDeletionService deletionService = new SafeFileDeletionService();
    private final AiSuggestionService aiService = new AiSuggestionService();
    private final DuplicateDetector duplicateDetector = new HashDuplicateDetector();

    private final List<FileMetadata> scannedFiles = Collections.synchronizedList(new ArrayList<>());

    public void setScannedFiles(List<FileMetadata> files) {
        synchronized (scannedFiles) {
            scannedFiles.clear();
            scannedFiles.addAll(files);
        }
    }


    public List<FileMetadata> getScannedFiles() {
        return new ArrayList<>(scannedFiles);
    }

    public List<FileMetadata> scanDirectory(Path root) throws IOException {
        scannedFiles.clear();
        CompletableFuture<Void> scanFuture = new CompletableFuture<>();

        fileScanner.startScan(root, new FileScanner.ScanListener() {
            @Override
            public void onFilesFound(List<FileMetadata> files) {
                scannedFiles.addAll(files);
            }

            @Override
            public void onDirectoryChanged(Path dir) {}

            @Override
            public void onProgress(long filesCount, long totalSize) {}

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
            scanFuture.get();
            return new ArrayList<>(scannedFiles);
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Scan failed", e);
        }
    }

    public Map<String, List<FileMetadata>> findDuplicates(List<FileMetadata> files) {
        return duplicateDetector.findDuplicates(files);
    }

    public ActionPlan getAiSuggestions() {
        return aiService.suggest(new ArrayList<>(scannedFiles));
    }

    public int executeCleanup(List<FileMetadata> files) {
        int count = 0;
        for (FileMetadata file : files) {
            try {
                if (deletionService.delete(file)) {
                    count++;
                }
            } catch (IOException ignored) {}
        }
        return count;
    }

    public List<FileMetadata> getLastScannedFiles() {
        return getScannedFiles();
    }
}
