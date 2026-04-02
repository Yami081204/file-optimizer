package com.fileoptimizer.core.trash;

import com.fileoptimizer.common.model.FileMetadata;
import java.io.IOException;
import java.nio.file.Path;


public interface TrashService {

    Path moveToTrash(FileMetadata file) throws IOException;


    void restore(Path trashPath, Path originalPath) throws IOException;
}
