package com.fileoptimizer.app.service;

import com.fileoptimizer.common.model.FileInsights;
import com.fileoptimizer.common.model.FileMetadata;
import com.fileoptimizer.core.scanner.FileScanner;
import com.fileoptimizer.core.scanner.FileScannerImpl;

import java.nio.file.Path;
import java.util.List;

public class FileInsightService {

    private final FileScanner scanner;

    public FileInsightService() {
        this.scanner = new FileScannerImpl();
    }

    public interface ScanObserver {
        void onFilesFound(List<FileMetadata> files);
        void onProgress(long totalFiles, long totalSize);
        void onCompleted(FileInsights insights);
        default void onDirectoryChanged(Path dir) {}
        default void onError(Throwable e) {}
    }

    public void scanAndObserve(Path rootPath, ScanObserver observer) {
        scanner.startScan(rootPath, new FileScanner.ScanListener() {
            @Override
            public void onFilesFound(List<FileMetadata> files) {
                if (observer != null) observer.onFilesFound(files);
            }

            @Override
            public void onProgress(long totalFiles, long totalSize) {
                if (observer != null) observer.onProgress(totalFiles, totalSize);
            }

            @Override
            public void onCompleted(FileInsights insights) {
                if (observer != null) observer.onCompleted(insights);
            }

            @Override
            public void onDirectoryChanged(Path dir) {
                if (observer != null) observer.onDirectoryChanged(dir);
            }

            @Override
            public void onError(Throwable t) {
                if (observer != null) observer.onError(t);
            }
        });
    }

    public void cancelScan() {
        scanner.cancelScan();
    }

    public boolean isScanning() {
        return scanner.isScanning();
    }
}
