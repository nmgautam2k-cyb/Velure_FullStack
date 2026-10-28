package com.ecommerce.controller;

import com.ecommerce.dto.UploadResponse;
import com.ecommerce.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/uploads")
public class AdminUploadController {

    private final FileStorageService storage;

    public AdminUploadController(FileStorageService storage) {
        this.storage = storage;
    }

    /** multipart/form-data with a "file" part. Returns the public URL to save as the product image. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new UploadResponse(storage.storeImage(file)));
    }
}
