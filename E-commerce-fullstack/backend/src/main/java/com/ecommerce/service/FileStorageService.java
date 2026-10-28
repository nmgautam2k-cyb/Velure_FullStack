package com.ecommerce.service;

import com.ecommerce.config.AppProperties;
import com.ecommerce.exception.BadRequestException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores uploaded product images on disk (app.upload.dir) and returns a public URL.
 * Only the database column image_url holds the URL - never the image bytes.
 *
 * The file type is decided from the file's own bytes (magic numbers), not from the filename or the
 * Content-Type header the client claims, and the stored name is a random UUID, so a crafted filename
 * cannot escape the upload directory or smuggle in an .html/.js file.
 */
@Service
public class FileStorageService {

    private final Path root;
    private final String publicBaseUrl;

    public FileStorageService(AppProperties props) {
        this.root = Path.of(props.upload().dir()).toAbsolutePath().normalize();
        String base = props.publicBaseUrl() == null ? "" : props.publicBaseUrl().trim();
        this.publicBaseUrl = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create upload directory " + root, e);
        }
    }

    public Path root() {
        return root;
    }

    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose an image file");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Could not read the uploaded file");
        }
        String extension = detectImageExtension(bytes);
        if (extension == null) {
            throw new BadRequestException("Only JPEG, PNG, GIF, WebP or AVIF images are allowed");
        }
        String filename = UUID.randomUUID() + "." + extension;
        Path target = root.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            throw new BadRequestException("Invalid file name");
        }
        try {
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
        return publicBaseUrl + "/uploads/" + filename;
    }

    /** Returns jpg/png/gif/webp/avif based on the leading bytes, or null if it is none of them. */
    static String detectImageExtension(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        if (b.length >= 6 && ascii(b, 0, 6).startsWith("GIF8")
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a') {
            return "gif";
        }
        if (b.length >= 12 && ascii(b, 0, 4).equals("RIFF") && ascii(b, 8, 12).equals("WEBP")) {
            return "webp";
        }
        if (b.length >= 12 && ascii(b, 4, 8).equals("ftyp")) {
            String brand = ascii(b, 8, 12);
            if (brand.equals("avif") || brand.equals("avis")) {
                return "avif";
            }
        }
        return null;
    }

    private static String ascii(byte[] b, int from, int to) {
        return new String(b, from, to - from, StandardCharsets.ISO_8859_1);
    }
}
