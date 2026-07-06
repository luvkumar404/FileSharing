package com.example.java.share.controller;

import com.example.java.share.dto.FileResponse;
import com.example.java.share.dto.FileUrlResponse;
import com.example.java.share.dto.ShareFileRequest;
import com.example.java.share.dto.ShareFileResponse;
import com.example.java.share.security.CustomUserDetails;
import com.example.java.share.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> upload(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileStorageService.upload(file, currentUser.getUser()));
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> listFiles(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(fileStorageService.listUserFiles(currentUser.getUser()));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<FileUrlResponse> download(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(fileStorageService.downloadOwnFile(id, currentUser.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        fileStorageService.deleteOwnFile(id, currentUser.getUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<ShareFileResponse> share(
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody ShareFileRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(fileStorageService.shareFile(id, request, currentUser.getUser(), servletRequest));
    }
}
