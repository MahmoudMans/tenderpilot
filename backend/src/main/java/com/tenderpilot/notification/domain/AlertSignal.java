package com.tenderpilot.notification.domain;

import java.util.UUID;

public record AlertSignal(

        String fingerprint,

        NotificationType type,

        NotificationSeverity severity,

        String title,

        String detail,

        UUID tenderId,

        String href

) {
}
