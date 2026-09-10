package com.tenderpilot.notification.api;

import com.tenderpilot.notification.domain.NotificationSeverity;
import com.tenderpilot.notification.domain.NotificationStatus;
import com.tenderpilot.notification.domain.NotificationType;
import com.tenderpilot.notification.domain.WatchlistTargetType;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

public record NotificationCenterResponse(

        Summary summary,

        List<NotificationItem> notifications,

        List<WatchlistItem> watchlist

) {

    public record Summary(

            int activeCount,

            int unreadCount,

            int criticalCount,

            int highCount,

            int watchlistCount

    ) {
    }


    public record NotificationItem(

            UUID id,

            NotificationType type,

            NotificationSeverity severity,

            String title,

            String detail,

            UUID tenderId,

            String href,

            NotificationStatus status,

            boolean active,

            Instant createdAt,

            Instant updatedAt,

            Instant lastObservedAt

    ) {
    }


    public record WatchlistItem(

            UUID id,

            WatchlistTargetType type,

            String targetKey,

            String label,

            Instant createdAt

    ) {
    }

}
