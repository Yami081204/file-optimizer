package com.fileoptimizer.core.scheduler;

import com.fileoptimizer.app.service.AutoCleanService;
import com.fileoptimizer.logging.ActionLogger;
import com.fileoptimizer.logging.impl.FileActionLogger;
import javafx.concurrent.Task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class CleanupScheduler {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true); // Ensure scheduler doesn't prevent app shutdown
        thread.setName("CleanupScheduler-Thread");
        return thread;
    });

    private final AutoCleanService autoCleanService = new AutoCleanService();
    private final ActionLogger logger = new FileActionLogger();
    private ScheduledFuture<?> currentTask;

    public void scheduleTask(TaskConfig config) {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(false);
        }

        if (!config.isEnabled()) return;

        Runnable cleanupRunnable = () -> {
            try {
                logger.log("SCHEDULED_CLEAN_START", "Starting background cleanup for: " + config.getTargetPath());

                
                Task<Integer> task = autoCleanService.createCleanTask(config.getTargetPath(), msg -> logger.log("SCHEDULED_CLEAN_STATUS", msg));

                task.run(); 
                
                Integer deletedCount = task.get();
                logger.log("SCHEDULED_CLEAN_FINISH", "Cleaned " + deletedCount + " files automatically.");
                
            } catch (Exception e) {
                logger.log("SCHEDULED_CLEAN_ERROR", "Error during background cleanup: " + e.getMessage());
            }
        };

        currentTask = scheduler.scheduleAtFixedRate(
                cleanupRunnable, 
                config.getInterval(), 
                config.getInterval(), 
                config.getTimeUnit()
        );

        logger.log("SCHEDULER_CONFIGURED", "Cleanup scheduled every " + config.getInterval() + " " + config.getTimeUnit());
    }


    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
