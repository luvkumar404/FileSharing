package com.example.java.share.dto;

public record FileUrlResponse(
        Long id,
        String originalFileName,
        String secureUrl
) {
}
