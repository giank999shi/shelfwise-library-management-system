package com.school.library.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

@Service
public class BookImageStorageService {

    private final Path uploadDirectory;

    public BookImageStorageService(@Value("${app.upload-dir:uploads/books}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) return null;
        if (image.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException("Book cover must be 5 MB or smaller.");
        }

        String contentType = image.getContentType() == null ? "" : image.getContentType().toLowerCase(Locale.ROOT);
        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Only JPG, PNG, and WEBP book covers are allowed.");
        };

        Files.createDirectories(uploadDirectory);
        String filename = UUID.randomUUID() + extension;
        Files.copy(image.getInputStream(), uploadDirectory.resolve(filename));
        return "/uploads/books/" + filename;
    }

    public void delete(String imagePath) {
        if (imagePath == null || !imagePath.startsWith("/uploads/books/")) return;
        String filename = imagePath.substring("/uploads/books/".length());
        if (filename.contains("/") || filename.contains("\\")) return;
        try { Files.deleteIfExists(uploadDirectory.resolve(filename).normalize()); }
        catch (IOException ignored) { /* A stale cover file must not block book management. */ }
    }
}
