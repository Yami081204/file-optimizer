package com.fileoptimizer.core.duplicate;

import com.fileoptimizer.common.model.FileMetadata;
import java.util.List;
import java.util.Map;


public interface DuplicateDetector {
    Map<String, List<FileMetadata>> findDuplicates(List<FileMetadata> files);
}
