package com.fileoptimizer.core.deletion;

import com.fileoptimizer.common.model.FileMetadata;
import com.fileoptimizer.core.safety.SafetyValidator;
import com.fileoptimizer.core.trash.TrashService;
import com.fileoptimizer.core.trash.TrashServiceImpl;
import com.fileoptimizer.logging.ActionLogger;
import com.fileoptimizer.logging.impl.FileActionLogger;

import java.io.IOException;

public class SafeFileDeletionService implements FileDeletionService {

    private final SafetyValidator safetyValidator = new SafetyValidator();
    private final TrashService trashService = new TrashServiceImpl();
    private final ActionLogger logger = new FileActionLogger();

    @Override
    public boolean delete(FileMetadata file) throws IOException {
        if (file == null || file.getPath() == null) {
            return false;
        }

        // 1. Safety validation
        if (!safetyValidator.isSafeToDelete(file)) {
            logger.log("SAFETY_FAILURE", "Cannot delete: " + file.getPath() + " (Protected/Hidden/System)");
            return false;
        }

        // 2. Perform safe deletion (Move to Trash)
        try {
            trashService.moveToTrash(file);
            logger.log("SAFE_DELETE_SUCCESS", "Moved to Trash: " + file.getPath());
            return true;
        } catch (IOException e) {
            logger.log("SAFE_DELETE_FAILURE", "Error: " + e.getMessage() + " for file: " + file.getPath());
            throw e;
        }
    }
}
