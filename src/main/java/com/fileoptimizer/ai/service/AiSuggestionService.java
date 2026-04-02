package com.fileoptimizer.ai.service;

import com.fileoptimizer.common.model.ActionPlan;
import com.fileoptimizer.common.model.FileCategory;
import com.fileoptimizer.common.model.FileMetadata;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class AiSuggestionService {

    private static final int DELETE_THRESHOLD = 70;
    private static final int MOVE_THRESHOLD = 40;

    private static final long LARGE_FILE_SIZE = 1024L * 1024 * 500; // 500 MB
    private static final long HUGE_FILE_SIZE = 1024L * 1024 * 1024; // 1 GB

    private static final int OLD_DAYS = 90; // 3 months
    private static final int VERY_OLD_DAYS = 180; // 6 months

    public ActionPlan suggest(List<FileMetadata> files) {
        ActionPlan plan = new ActionPlan();

        for (FileMetadata file : files) {
            double finalScore = calculateScore(file);
            file.setScore(finalScore); // Update the model score for UI feedback

            if (finalScore >= DELETE_THRESHOLD) {
                plan.addFileToDelete(file);
            } else if (finalScore >= MOVE_THRESHOLD) {
                plan.addFileToMove(file);
            }
        }

        plan.getFilesToDelete().sort(Comparator.comparingDouble(FileMetadata::getScore).reversed());
        plan.getFilesToMove().sort(Comparator.comparingDouble(FileMetadata::getScore).reversed());

        return plan;
    }


    private double calculateScore(FileMetadata file) {
        double score = 0;

        if (file.getCategory() == FileCategory.TEMP) {
            score += 50;
        } else if (file.getCategory() == FileCategory.DUPLICATE) {
            score += 65;
        } else if (file.getCategory() == FileCategory.ARCHIVE) {
            score += 10;
        }

        if (file.getModifiedDate() != null) {
            long daysOld = ChronoUnit.DAYS.between(file.getModifiedDate(), LocalDateTime.now());
            if (daysOld > VERY_OLD_DAYS) {
                score += 30;
            } else if (daysOld > OLD_DAYS) {
                score += 15;
            }
        }

        if (file.getSize() > HUGE_FILE_SIZE) {
            score += 25;
        } else if (file.getSize() > LARGE_FILE_SIZE) {
            score += 15;
        }

        String ext = file.getExtension().toLowerCase();
        if (List.of("exe", "dll", "sys").contains(ext)) {
            score -= 40;
        }

        return Math.min(100, Math.max(0, score));
    }
}
