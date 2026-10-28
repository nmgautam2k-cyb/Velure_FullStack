package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class FileStorageServiceTest {

    @Test
    void detectsRealImageTypesFromBytes() {
        assertEquals("jpg", FileStorageService.detectImageExtension(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}));
        assertEquals("png", FileStorageService.detectImageExtension(
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0}));
        assertEquals("gif", FileStorageService.detectImageExtension("GIF89a....".getBytes(StandardCharsets.ISO_8859_1)));
        assertEquals("webp", FileStorageService.detectImageExtension("RIFF....WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1)));
    }

    @Test
    void rejectsNonImages() {
        assertNull(FileStorageService.detectImageExtension("<html><script>alert(1)</script></html>".getBytes(StandardCharsets.UTF_8)));
        assertNull(FileStorageService.detectImageExtension(new byte[0]));
    }
}
