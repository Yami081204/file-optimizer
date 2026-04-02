package com.fileoptimizer.core.plugin;

import com.fileoptimizer.common.model.FileMetadata;
import java.util.List;


public interface Plugin {

    void init();

    String execute(List<FileMetadata> files);

    String getName();

    String getDescription();
}
