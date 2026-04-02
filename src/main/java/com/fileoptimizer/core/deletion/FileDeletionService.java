package com.fileoptimizer.core.deletion;

import com.fileoptimizer.common.model.FileMetadata;
import java.io.IOException;


public interface FileDeletionService {
    boolean delete(FileMetadata file) throws IOException;
}
