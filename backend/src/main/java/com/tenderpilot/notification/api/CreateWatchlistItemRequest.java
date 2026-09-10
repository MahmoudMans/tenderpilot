package com.tenderpilot.notification.api;

import com.tenderpilot.notification.domain.WatchlistTargetType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateWatchlistItemRequest(

        @NotNull
        WatchlistTargetType type,

        @NotBlank
        @Size(
                max = 512
        )
        String targetKey,

        @Size(
                max = 255
        )
        String label

) {
}
