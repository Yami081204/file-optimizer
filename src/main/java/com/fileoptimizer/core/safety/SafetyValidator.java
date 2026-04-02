package com.fileoptimizer.core.safety;

import com.fileoptimizer.common.model.FileMetadata;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class SafetyValidator {

    private static final List<String> PROTECTED_DIRECTORIES = Arrays.asList(
            "C:\\Windows",
            "C:\\Program Files",
            "C:\\Program Files (x86)",
            "C:\\Users\\Public",
            "C:\\Recovery",
            "C:\\System Volume Information",
            "/bin",
            "/sbin",
            "/etc",
            "/usr",
            "/System",
            "/Library"
    );

    /**
     * Checks if the given file is safe to delete.
     *
     * @param file The metadata of the file to check.
     * @return true if it's safe to delete, false otherwise.
     */
    public boolean isSafeToDelete(FileMetadata file) {
        if (file == null || file.getPath() == null) {
            return false;
        }
        
        Path path = file.getPath();

        // 1. Check if it's a system directory
        String absolutePath = path.toAbsolutePath().toString().toLowerCase();
        for (String protectedDir : PROTECTED_DIRECTORIES) {
            if (absolutePath.startsWith(protectedDir.toLowerCase())) {
                return false;
            }
        }

        // 2. Check if it's a hidden file
        try {
            if (Files.isHidden(path)) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        // 3. Check for write/delete permissions
        if (!Files.isWritable(path)) {
            return false;
        }

        // 4. Check if it's a directory
        if (Files.isDirectory(path)) {
            return false;
        }

        // 5. Try to check if file is in use (Attempt to acquire a lock)
        if (isFileInUse(path)) {
            return false;
        }

        return true;
    }

    private boolean isFileInUse(Path path) {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE);
             FileLock lock = channel.tryLock()) {
            return lock == null;
        } catch (IOException e) {
            // If we can't open it for writing, it's likely in use or restricted
            return true;
        }
    }
}
