package com.tenderpilot.notification.infrastructure;

import com.tenderpilot.notification.domain.WatchlistItemSnapshot;
import com.tenderpilot.notification.domain.WatchlistTargetType;

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
        name = "organization_watchlist_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_org_watchlist_target",
                        columnNames = {
                                "organization_id",
                                "type",
                                "target_key"
                        }
                )
        }
)
public class WatchlistItemEntity {

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


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            nullable = false,
            length = 32
    )
    private WatchlistTargetType type;


    @Column(
            name = "target_key",
            nullable = false,
            length = 512
    )
    private String targetKey;


    @Column(
            length = 255
    )
    private String label;


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    protected WatchlistItemEntity() {
    }


    public static WatchlistItemEntity create(
            UUID organizationId,
            WatchlistTargetType type,
            String targetKey,
            String label,
            Instant now
    ) {

        WatchlistItemEntity entity =
                new WatchlistItemEntity();


        entity.id =
                UUID.randomUUID();

        entity.organizationId =
                organizationId;

        entity.type =
                type;

        entity.targetKey =
                targetKey;

        entity.label =
                label;

        entity.createdAt =
                now;


        return entity;
    }


    public void updateLabel(
            String label
    ) {

        this.label =
                label;
    }


    public UUID id() {

        return id;
    }


    public UUID organizationId() {

        return organizationId;
    }


    public WatchlistTargetType type() {

        return type;
    }


    public String targetKey() {

        return targetKey;
    }


    public String label() {

        return label;
    }


    public Instant createdAt() {

        return createdAt;
    }


    public WatchlistItemSnapshot snapshot() {

        return new WatchlistItemSnapshot(
                id,
                type,
                targetKey,
                label
        );
    }

}
