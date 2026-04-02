package com.fileoptimizer.data.repository;

import com.fileoptimizer.common.model.FileCategory;
import com.fileoptimizer.data.entity.FileActionHistory;
import java.util.HashMap;
import java.util.Map;


public class FileRepository {

    // Key: Extension (e.g., ".tmp", ".jpg")
    private static final Map<String, FileActionHistory> behaviorMap = new HashMap<>();


    public FileActionHistory getHistory(String extension, FileCategory category) {
        return behaviorMap.computeIfAbsent(extension.toLowerCase(), 
                ext -> new FileActionHistory(ext, category));
    }


    public void recordDelete(String extension, FileCategory category) {
        getHistory(extension, category).incrementDelete();
    }


    public void recordKeep(String extension, FileCategory category) {
        getHistory(extension, category).incrementKeep();
    }

    public double getUserBias(String extension) {
        FileActionHistory history = behaviorMap.get(extension.toLowerCase());
        if (history == null || (history.getKeepCount() + history.getDeleteCount() == 0)) {
            return 0;
        }

        int total = history.getKeepCount() + history.getDeleteCount();
        double deleteRate = (double) history.getDeleteCount() / total;
        
        // Map delete rate (0.0 to 1.0) to bias (-50 to +50)
        return (deleteRate - 0.5) * 100;
    }
}
