package com.tenderpilot.notification.domain;

import java.util.UUID;

public record WatchlistItemSnapshot(

        UUID id,

        WatchlistTargetType type,

        String targetKey,

        String label

) {
}
