package com.fileoptimizer.common.model;

import java.util.ArrayList;
import java.util.List;

public class ActionPlan {
    private List<FileMetadata> filesToDelete = new ArrayList<>();
    private List<FileMetadata> filesToMove = new ArrayList<>();

    public ActionPlan() {}

    public ActionPlan(List<FileMetadata> filesToDelete, List<FileMetadata> filesToMove) {
        this.filesToDelete = filesToDelete != null ? filesToDelete : new ArrayList<>();
        this.filesToMove = filesToMove != null ? filesToMove : new ArrayList<>();
    }

    public List<FileMetadata> getFilesToDelete() {
        return filesToDelete;
    }

    public void setFilesToDelete(List<FileMetadata> filesToDelete) {
        this.filesToDelete = filesToDelete;
    }

    public List<FileMetadata> getFilesToMove() {
        return filesToMove;
    }

    public void setFilesToMove(List<FileMetadata> filesToMove) {
        this.filesToMove = filesToMove;
    }

    public void addFileToDelete(FileMetadata file) {
        this.filesToDelete.add(file);
    }

    public void addFileToMove(FileMetadata file) {
        this.filesToMove.add(file);
    }

    public long getTotalFilesAffected() {
        return filesToDelete.size() + filesToMove.size();
    }
}
