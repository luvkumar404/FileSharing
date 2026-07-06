package com.example.java.share.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ShareFileRequest(
        @NotNull(message = "Expiration time is required")
        @Min(value = 1, message = "Expiration must be at least 1 minute")
        @Max(value = 10080, message = "Expiration cannot be more than 7 days")
        Integer expiresInMinutes
) {
}
