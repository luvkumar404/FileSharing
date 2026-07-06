package com.example.java.share.controller;

import com.example.java.share.entity.FileEntity;
import com.example.java.share.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final FileStorageService fileStorageService;

    @GetMapping("/{token}")
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable String token) {
        FileEntity file = fileStorageService.getSharedFile(token);
        Resource resource = fileStorageService.downloadSharedFile(token);
        return FileController.buildDownloadResponse(resource, file);
    }
}
