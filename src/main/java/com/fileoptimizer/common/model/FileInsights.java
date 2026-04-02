package com.fileoptimizer.common.model;

import java.time.LocalDate;
import java.util.*;

public class FileInsights {

    private long totalFiles;
    private long totalSize;

    // 👉 dùng EnumMap là đúng (nhanh + gọn)
    private Map<FileCategory, Long> sizeByCategory = new EnumMap<>(FileCategory.class);

    private List<FileMetadata> topLargestFiles = new ArrayList<>();
    private Map<LocalDate, Long> growthOverTime = new TreeMap<>();

    public FileInsights() {
        initCategories();
    }

    private void initCategories() {
        for (FileCategory category : FileCategory.values()) {
            sizeByCategory.put(category, 0L);
        }
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(long totalFiles) {
        this.totalFiles = totalFiles;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public Map<FileCategory, Long> getSizeByCategory() {
        return sizeByCategory;
    }

    // 👉 FIX QUAN TRỌNG (bạn đang thiếu)
    public void setSizeByCategory(Map<FileCategory, Long> map) {
        this.sizeByCategory.clear();
        initCategories(); // reset

        if (map != null) {
            map.forEach((k, v) ->
                    this.sizeByCategory.merge(k, v, Long::sum)
            );
        }
    }

    public List<FileMetadata> getTopLargestFiles() {
        return topLargestFiles;
    }

    public void setTopLargestFiles(List<FileMetadata> topLargestFiles) {
        this.topLargestFiles = topLargestFiles;
    }

    public Map<LocalDate, Long> getGrowthOverTime() {
        return growthOverTime;
    }

    public void setGrowthOverTime(Map<LocalDate, Long> growthOverTime) {
        this.growthOverTime = growthOverTime;
    }

    public void addFile(FileMetadata file) {
        if (file == null) return;

        this.totalFiles++;
        this.totalSize += file.getSize();

        FileCategory category = file.getCategory() != null
                ? file.getCategory()
                : FileCategory.OTHER;

        this.sizeByCategory.merge(category, file.getSize(), Long::sum);

        if (file.getModifiedDate() != null) {
            LocalDate date = file.getModifiedDate().toLocalDate();
            growthOverTime.merge(date, 1L, Long::sum);
        }

        topLargestFiles.add(file);
        topLargestFiles.sort((a, b) -> Long.compare(b.getSize(), a.getSize()));

        if (topLargestFiles.size() > 10) {
            topLargestFiles = topLargestFiles.subList(0, 10);
        }
    }
    public void addSize(FileCategory category, long size) {
        if (category == null) category = FileCategory.OTHER;

        sizeByCategory.merge(category, size, Long::sum);
        totalSize += size;
    }

    public void setFreeSpace(long freeSpace) {
        sizeByCategory.put(FileCategory.FREE, freeSpace);
    }
}