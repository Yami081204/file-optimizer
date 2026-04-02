package com.fileoptimizer.core.scoring;

import com.fileoptimizer.common.model.FileMetadata;
import com.fileoptimizer.data.repository.FileRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


public class FileScorer {

    private final FileRepository repository = new FileRepository();

    public double calculateScore(FileMetadata file) {
        double score = 0;

        if (file.getModifiedDate() != null) {
            long daysOld = ChronoUnit.DAYS.between(file.getModifiedDate(), LocalDateTime.now());
            if (daysOld > 180) score += 30; // 6 months old
            else if (daysOld > 90) score += 15; // 3 months old
        }

        if (file.getSize() > 1024L * 1024 * 1024) score += 20; // > 1GB
        else if (file.getSize() > 1024L * 1024 * 100) score += 10; // > 100MB

        switch (file.getCategory()) {
            case TEMP: score += 50; break;
            case DUPLICATE: score += 65; break;
            case ARCHIVE: score += 10; break;
            default: break;
        }

        double userBias = repository.getUserBias(file.getExtension());
        score += userBias;

        return Math.min(100, Math.max(0, score));
    }


    public void recordAction(FileMetadata file, boolean wasDeleted) {
        if (wasDeleted) {
            repository.recordDelete(file.getExtension(), file.getCategory());
        } else {
            repository.recordKeep(file.getExtension(), file.getCategory());
        }
    }
}
