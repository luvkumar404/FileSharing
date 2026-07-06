package com.example.java.share.dto;

import java.time.LocalDateTime;

public record FileResponse(
        Long id,
        String originalFileName,
        String storedFileName,
        String fileType,
        Long fileSize,
        LocalDateTime createdAt
) {
}
