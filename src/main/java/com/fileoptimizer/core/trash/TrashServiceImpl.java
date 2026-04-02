package com.fileoptimizer.core.trash;

import com.fileoptimizer.common.model.FileMetadata;
import com.fileoptimizer.logging.ActionLogger;
import com.fileoptimizer.logging.impl.FileActionLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;


public class TrashServiceImpl implements TrashService {

    private final ActionLogger logger = new FileActionLogger();
    private static final String TRASH_DIR_NAME = "file-optimizer-trash";
    private final Path globalTrashDir;

    private final Map<Path, Path> restoreMap = new HashMap<>();

    public TrashServiceImpl() {
        this.globalTrashDir = Paths.get(System.getProperty("user.home"), TRASH_DIR_NAME);
        try {
            if (!Files.exists(globalTrashDir)) {
                Files.createDirectories(globalTrashDir);
            }
        } catch (IOException e) {
            System.err.println("Could not create trash directory: " + e.getMessage());
        }
    }

    @Override
    public Path moveToTrash(FileMetadata file) throws IOException {
        Path sourcePath = file.getPath();
        if (!Files.exists(sourcePath)) {
            throw new IOException("Source file does not exist: " + sourcePath);
        }

        String uniqueName = System.currentTimeMillis() + "_" + sourcePath.getFileName().toString();
        Path targetPath = globalTrashDir.resolve(uniqueName);

        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        restoreMap.put(targetPath, sourcePath);
        
        logger.log("MOVE_TO_INTERNAL_TRASH", "From: " + sourcePath + " To: " + targetPath);
        return targetPath;
    }

    @Override
    public void restore(Path trashPath, Path originalPath) throws IOException {
        if (!Files.exists(trashPath)) {
            throw new IOException("Trash file does not exist: " + trashPath);
        }

        // Ensure parent directories exist
        if (originalPath.getParent() != null && !Files.exists(originalPath.getParent())) {
            Files.createDirectories(originalPath.getParent());
        }

        Files.move(trashPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
        restoreMap.remove(trashPath);
        
        logger.log("RESTORE_SUCCESS", "Restored to: " + originalPath);
    }

    public Map<Path, Path> getRestoreMap() {
        return restoreMap;
    }
}
