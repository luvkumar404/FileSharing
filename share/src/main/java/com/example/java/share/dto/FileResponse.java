package com.example.java.share.dto;

import java.time.LocalDateTime;

public record FileResponse(
        Long id,
        String originalFileName,
        String fileType,
        Long fileSize,
        String cloudinaryPublicId,
        String cloudinarySecureUrl,
        LocalDateTime createdAt
) {
}
