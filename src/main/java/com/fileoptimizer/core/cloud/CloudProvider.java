package com.fileoptimizer.core.cloud;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;


public interface CloudProvider {
    String getName();
    CompletableFuture<Boolean> authenticate();
    CompletableFuture<Void> uploadFile(Path localPath, String remoteDir);
    CompletableFuture<Void> downloadFile(String remotePath, Path localDir);
}
