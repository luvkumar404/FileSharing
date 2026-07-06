package com.example.java.share.dto;

import java.time.LocalDateTime;

public record ShareFileResponse(
        String token,
        String publicUrl,
        LocalDateTime expiresAt
) {
}
