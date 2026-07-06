package com.example.java.share.controller;

import com.example.java.share.dto.FileUrlResponse;
import com.example.java.share.service.FileStorageService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<FileUrlResponse> downloadSharedFile(@PathVariable String token) {
        return ResponseEntity.ok(fileStorageService.downloadSharedFile(token));
    }
}
