package com.tenderpilot.notification.infrastructure;

import com.tenderpilot.notification.domain.WatchlistTargetType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistItemRepository
        extends JpaRepository<WatchlistItemEntity, UUID> {

    List<WatchlistItemEntity>
    findAllByOrganizationIdOrderByCreatedAtDesc(
            UUID organizationId
    );


    Optional<WatchlistItemEntity>
    findByOrganizationIdAndTypeAndTargetKey(
            UUID organizationId,
            WatchlistTargetType type,
            String targetKey
    );


    long deleteByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

}
