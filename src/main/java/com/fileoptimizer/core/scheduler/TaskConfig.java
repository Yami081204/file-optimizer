package com.fileoptimizer.core.scheduler;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for scheduled cleanup tasks.
 */
public class TaskConfig {
    private Path targetPath;
    private long interval;
    private TimeUnit timeUnit;
    private boolean enabled;

    public TaskConfig(Path targetPath, long interval, TimeUnit timeUnit) {
        this.targetPath = targetPath;
        this.interval = interval;
        this.timeUnit = timeUnit;
        this.enabled = true;
    }

    public Path getTargetPath() { return targetPath; }
    public long getInterval() { return interval; }
    public TimeUnit getTimeUnit() { return timeUnit; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
