package com.fileoptimizer.core.scanner;

import com.fileoptimizer.common.model.FileInsights;
import com.fileoptimizer.common.model.FileMetadata;
import java.nio.file.Path;
import java.util.List;


public interface FileScanner {

    interface ScanListener {

        void onFilesFound(List<FileMetadata> files);
        
        void onDirectoryChanged(Path dir);
        
        void onProgress(long filesCount, long totalSize);
        
        void onCompleted(FileInsights insights);

        void onError(Throwable t);
    }


    void startScan(Path root, ScanListener listener);


    void cancelScan();


    boolean isScanning();
}
