package com.fileoptimizer.core.duplicate;

import com.fileoptimizer.common.model.FileMetadata;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class HashDuplicateDetector implements DuplicateDetector {

    private static final int BUFFER_SIZE = 8192;

    @Override
    public Map<String, List<FileMetadata>> findDuplicates(List<FileMetadata> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<FileMetadata>> bySize = files.stream()
                .collect(Collectors.groupingBy(FileMetadata::getSize));

        Map<String, List<FileMetadata>> duplicatesByHash = new HashMap<>();

        for (List<FileMetadata> sizeGroup : bySize.values()) {
            if (sizeGroup.size() < 2) continue;

            for (FileMetadata file : sizeGroup) {
                try {
                    String hash = calculateHash(file);
                    duplicatesByHash.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.err.println("Error calculating hash for " + file.getPath() + ": " + e.getMessage());
                }
            }
        }

        return duplicatesByHash.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1 && !entry.getKey().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String calculateHash(FileMetadata file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(file.getPath()))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        return bytesToHex(digest.digest());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
