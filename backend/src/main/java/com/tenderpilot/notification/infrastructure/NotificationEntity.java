package com.tenderpilot.notification.infrastructure;

import com.tenderpilot.notification.domain.AlertSignal;
import com.tenderpilot.notification.domain.NotificationSeverity;
import com.tenderpilot.notification.domain.NotificationStatus;
import com.tenderpilot.notification.domain.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

import java.util.UUID;

@Entity
@Table(
        name = "organization_notifications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_org_notification_fingerprint",
                        columnNames = {
                                "organization_id",
                                "fingerprint"
                        }
                )
        }
)
public class NotificationEntity {

    @Id
    @Column(
            nullable = false
    )
    private UUID id;


    @Column(
            name = "organization_id",
            nullable = false
    )
    private UUID organizationId;


    @Column(
            nullable = false,
            length = 700
    )
    private String fingerprint;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            nullable = false,
            length = 64
    )
    private NotificationType type;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            nullable = false,
            length = 16
    )
    private NotificationSeverity severity;


    @Column(
            nullable = false,
            length = 255
    )
    private String title;


    @Column(
            nullable = false,
            columnDefinition = "text"
    )
    private String detail;


    @Column(
            name = "tender_id"
    )
    private UUID tenderId;


    @Column(
            nullable = false,
            length = 512
    )
    private String href;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            nullable = false,
            length = 24
    )
    private NotificationStatus status;


    @Column(
            nullable = false
    )
    private boolean active;


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;


    @Column(
            name = "last_observed_at",
            nullable = false
    )
    private Instant lastObservedAt;


    protected NotificationEntity() {
    }


    public static NotificationEntity create(
            UUID organizationId,
            AlertSignal signal,
            Instant now
    ) {

        NotificationEntity entity =
                new NotificationEntity();


        entity.id =
                UUID.randomUUID();

        entity.organizationId =
                organizationId;

        entity.fingerprint =
                signal.fingerprint();

        entity.type =
                signal.type();

        entity.severity =
                signal.severity();

        entity.title =
                signal.title();

        entity.detail =
                signal.detail();

        entity.tenderId =
                signal.tenderId();

        entity.href =
                signal.href();

        entity.status =
                NotificationStatus.UNREAD;

        entity.active =
                true;

        entity.createdAt =
                now;

        entity.updatedAt =
                now;

        entity.lastObservedAt =
                now;


        return entity;
    }


    public void observe(
            AlertSignal signal,
            Instant now
    ) {

        boolean previouslyActive =
                active;


        type =
                signal.type();

        severity =
                signal.severity();

        title =
                signal.title();

        detail =
                signal.detail();

        tenderId =
                signal.tenderId();

        href =
                signal.href();

        active =
                true;

        lastObservedAt =
                now;

        updatedAt =
                now;


        if (
                !previouslyActive
                &&
                status == NotificationStatus.READ
        ) {

            status =
                    NotificationStatus.UNREAD;
        }
    }


    public void markInactive(
            Instant now
    ) {

        if (
                active
        ) {

            active =
                    false;

            updatedAt =
                    now;
        }
    }


    public void acknowledge(
            Instant now
    ) {

        if (
                status != NotificationStatus.DISMISSED
        ) {

            status =
                    NotificationStatus.READ;

            updatedAt =
                    now;
        }
    }


    public void dismiss(
            Instant now
    ) {

        status =
                NotificationStatus.DISMISSED;

        active =
                false;

        updatedAt =
                now;
    }


    public UUID id() {

        return id;
    }


    public UUID organizationId() {

        return organizationId;
    }


    public String fingerprint() {

        return fingerprint;
    }


    public NotificationType type() {

        return type;
    }


    public NotificationSeverity severity() {

        return severity;
    }


    public String title() {

        return title;
    }


    public String detail() {

        return detail;
    }


    public UUID tenderId() {

        return tenderId;
    }


    public String href() {

        return href;
    }


    public NotificationStatus status() {

        return status;
    }


    public boolean active() {

        return active;
    }


    public Instant createdAt() {

        return createdAt;
    }


    public Instant updatedAt() {

        return updatedAt;
    }


    public Instant lastObservedAt() {

        return lastObservedAt;
    }

}
