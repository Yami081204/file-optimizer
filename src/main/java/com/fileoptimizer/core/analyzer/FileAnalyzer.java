package com.fileoptimizer.core.analyzer;

import com.fileoptimizer.common.model.FileMetadata;
import java.io.IOException;
import java.nio.file.Path;


public interface FileAnalyzer {
    FileMetadata analyze(Path path) throws IOException;
}
