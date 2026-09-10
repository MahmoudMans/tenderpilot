package com.tenderpilot.notification.application;

import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.dashboard.api.DashboardCommandCenterResponse;

import com.tenderpilot.dashboard.application.DashboardCommandCenterService;

import com.tenderpilot.notification.api.CreateWatchlistItemRequest;
import com.tenderpilot.notification.api.NotificationCenterResponse;

import com.tenderpilot.notification.domain.AlertSignal;
import com.tenderpilot.notification.domain.NotificationSeverity;
import com.tenderpilot.notification.domain.NotificationStatus;
import com.tenderpilot.notification.domain.WatchlistTargetType;

import com.tenderpilot.notification.infrastructure.NotificationEntity;
import com.tenderpilot.notification.infrastructure.NotificationRepository;
import com.tenderpilot.notification.infrastructure.WatchlistItemEntity;
import com.tenderpilot.notification.infrastructure.WatchlistItemRepository;

import java.time.Instant;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationCenterService {

    private final WatchlistItemRepository
            watchlistRepository;


    private final NotificationRepository
            notificationRepository;


    private final DashboardCommandCenterService
            dashboardService;


    private final NotificationSignalFactory
            signalFactory;


    public NotificationCenterService(
            WatchlistItemRepository watchlistRepository,
            NotificationRepository notificationRepository,
            DashboardCommandCenterService dashboardService,
            NotificationSignalFactory signalFactory
    ) {

        this.watchlistRepository =
                watchlistRepository;

        this.notificationRepository =
                notificationRepository;

        this.dashboardService =
                dashboardService;

        this.signalFactory =
                signalFactory;
    }


    @Transactional(
            readOnly = true
    )
    public NotificationCenterResponse center(
            UUID organizationId
    ) {

        return response(
                organizationId
        );
    }


    @Transactional
    public NotificationCenterResponse createWatchlistItem(
            UUID organizationId,
            CreateWatchlistItemRequest request
    ) {

        WatchlistTargetType type =
                request.type();


        String targetKey =
                normalizeTarget(
                        type,
                        request.targetKey()
                );


        String label =
                clean(
                        request.label()
                );


        WatchlistItemEntity entity =
                watchlistRepository
                        .findByOrganizationIdAndTypeAndTargetKey(
                                organizationId,
                                type,
                                targetKey
                        )
                        .orElseGet(
                                () ->
                                        WatchlistItemEntity.create(
                                                organizationId,
                                                type,
                                                targetKey,
                                                label,
                                                Instant.now()
                                        )
                        );


        entity.updateLabel(
                label
        );


        watchlistRepository.save(
                entity
        );


        return response(
                organizationId
        );
    }


    @Transactional
    public NotificationCenterResponse removeWatchlistItem(
            UUID organizationId,
            UUID watchlistId
    ) {

        long deleted =
                watchlistRepository
                        .deleteByIdAndOrganizationId(
                                watchlistId,
                                organizationId
                        );


        if (
                deleted == 0
        ) {

            throw new ResourceNotFoundException(
                    "Watchlist item was not found"
            );
        }


        return response(
                organizationId
        );
    }


    @Transactional
    public NotificationCenterResponse refresh(
            UUID organizationId
    ) {

        DashboardCommandCenterResponse dashboard =
                dashboardService
                        .commandCenter(
                                organizationId
                        );


        List<WatchlistItemEntity> watchlist =
                watchlistRepository
                        .findAllByOrganizationIdOrderByCreatedAtDesc(
                                organizationId
                        );


        List<AlertSignal> signals =
                signalFactory
                        .create(
                                dashboard,

                                watchlist
                                        .stream()
                                        .map(
                                                WatchlistItemEntity::snapshot
                                        )
                                        .toList()
                        );


        List<NotificationEntity> existing =
                notificationRepository
                        .findAllByOrganizationIdOrderByActiveDescLastObservedAtDescCreatedAtDesc(
                                organizationId
                        );


        Map<String, NotificationEntity> byFingerprint =
                new LinkedHashMap<>();


        for (
                NotificationEntity entity :
                existing
        ) {

            byFingerprint.put(
                    entity.fingerprint(),
                    entity
            );
        }


        Set<String> activeFingerprints =
                signals
                        .stream()
                        .map(
                                AlertSignal::fingerprint
                        )
                        .collect(
                                java.util.stream.Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );


        Instant now =
                Instant.now();


        for (
                NotificationEntity entity :
                existing
        ) {

            if (
                    !activeFingerprints.contains(
                            entity.fingerprint()
                    )
            ) {

                entity.markInactive(
                        now
                );
            }
        }


        for (
                AlertSignal signal :
                signals
        ) {

            NotificationEntity entity =
                    byFingerprint.get(
                            signal.fingerprint()
                    );


            if (
                    entity == null
            ) {

                entity =
                        NotificationEntity.create(
                                organizationId,
                                signal,
                                now
                        );


                existing =
                        append(
                                existing,
                                entity
                        );


            } else {

                entity.observe(
                        signal,
                        now
                );
            }
        }


        notificationRepository.saveAll(
                existing
        );


        return response(
                organizationId
        );
    }


    @Transactional
    public NotificationCenterResponse acknowledge(
            UUID organizationId,
            UUID notificationId
    ) {

        NotificationEntity entity =
                notification(
                        organizationId,
                        notificationId
                );


        entity.acknowledge(
                Instant.now()
        );


        notificationRepository.save(
                entity
        );


        return response(
                organizationId
        );
    }


    @Transactional
    public NotificationCenterResponse dismiss(
            UUID organizationId,
            UUID notificationId
    ) {

        NotificationEntity entity =
                notification(
                        organizationId,
                        notificationId
                );


        entity.dismiss(
                Instant.now()
        );


        notificationRepository.save(
                entity
        );


        return response(
                organizationId
        );
    }


    @Transactional
    public NotificationCenterResponse markAllRead(
            UUID organizationId
    ) {

        List<NotificationEntity> notifications =
                notificationRepository
                        .findAllByOrganizationIdOrderByActiveDescLastObservedAtDescCreatedAtDesc(
                                organizationId
                        );


        Instant now =
                Instant.now();


        for (
                NotificationEntity entity :
                notifications
        ) {

            if (
                    entity.status()
                            ==
                            NotificationStatus.UNREAD
            ) {

                entity.acknowledge(
                        now
                );
            }
        }


        notificationRepository.saveAll(
                notifications
        );


        return response(
                organizationId
        );
    }


    private NotificationEntity notification(
            UUID organizationId,
            UUID notificationId
    ) {

        return notificationRepository
                .findByIdAndOrganizationId(
                        notificationId,
                        organizationId
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Notification was not found"
                                )
                );
    }


    private NotificationCenterResponse response(
            UUID organizationId
    ) {

        List<NotificationEntity> notifications =
                notificationRepository
                        .findAllByOrganizationIdOrderByActiveDescLastObservedAtDescCreatedAtDesc(
                                organizationId
                        );


        List<WatchlistItemEntity> watchlist =
                watchlistRepository
                        .findAllByOrganizationIdOrderByCreatedAtDesc(
                                organizationId
                        );


        int active =
                (int)
                        notifications
                                .stream()
                                .filter(
                                        NotificationEntity::active
                                )
                                .count();


        int unread =
                (int)
                        notifications
                                .stream()
                                .filter(
                                        NotificationEntity::active
                                )
                                .filter(
                                        item ->
                                                item.status()
                                                        ==
                                                        NotificationStatus.UNREAD
                                )
                                .count();


        int critical =
                activeSeverityCount(
                        notifications,
                        NotificationSeverity.CRITICAL
                );


        int high =
                activeSeverityCount(
                        notifications,
                        NotificationSeverity.HIGH
                );


        return new NotificationCenterResponse(
                new NotificationCenterResponse
                        .Summary(
                                active,
                                unread,
                                critical,
                                high,
                                watchlist.size()
                        ),

                notifications
                        .stream()
                        .map(
                                this::notificationResponse
                        )
                        .toList(),

                watchlist
                        .stream()
                        .map(
                                this::watchlistResponse
                        )
                        .toList()
        );
    }


    private int activeSeverityCount(
            List<NotificationEntity> notifications,
            NotificationSeverity severity
    ) {

        return (int)
                notifications
                        .stream()
                        .filter(
                                NotificationEntity::active
                        )
                        .filter(
                                notification ->
                                        notification.severity()
                                                ==
                                                severity
                        )
                        .count();
    }


    private NotificationCenterResponse.NotificationItem
    notificationResponse(
            NotificationEntity entity
    ) {

        return new NotificationCenterResponse
                .NotificationItem(
                        entity.id(),
                        entity.type(),
                        entity.severity(),
                        entity.title(),
                        entity.detail(),
                        entity.tenderId(),
                        entity.href(),
                        entity.status(),
                        entity.active(),
                        entity.createdAt(),
                        entity.updatedAt(),
                        entity.lastObservedAt()
                );
    }


    private NotificationCenterResponse.WatchlistItem
    watchlistResponse(
            WatchlistItemEntity entity
    ) {

        return new NotificationCenterResponse
                .WatchlistItem(
                        entity.id(),
                        entity.type(),
                        entity.targetKey(),
                        entity.label(),
                        entity.createdAt()
                );
    }


    private String normalizeTarget(
            WatchlistTargetType type,
            String raw
    ) {

        String value =
                clean(
                        raw
                );


        if (
                value == null
        ) {

            throw new InvalidRequestException(
                    "Watchlist target is required"
            );
        }


        return switch (
                type
        ) {

            case TENDER, BUYER ->
                    normalizeUuid(
                            value
                    );

            case CPV ->
                    normalizeCpv(
                            value
                    );
        };
    }


    private String normalizeUuid(
            String value
    ) {

        try {

            return UUID
                    .fromString(
                            value
                    )
                    .toString();


        } catch (
                IllegalArgumentException exception
        ) {

            throw new InvalidRequestException(
                    "Watchlist target must be a valid UUID"
            );
        }
    }


    private String normalizeCpv(
            String value
    ) {

        String result =
                value.replaceAll(
                        "\\s+",
                        ""
                );


        if (
                !result.matches(
                        "[0-9]{1,8}"
                )
        ) {

            throw new InvalidRequestException(
                    "CPV watchlist targets must contain between 1 and 8 digits"
            );
        }


        return result;
    }


    private String clean(
            String value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        String result =
                value
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        return result.isBlank()
                ? null
                : result;
    }


    private List<NotificationEntity> append(
            List<NotificationEntity> source,
            NotificationEntity item
    ) {

        java.util.ArrayList<NotificationEntity> result =
                new java.util.ArrayList<>(
                        source
                );


        result.add(
                item
        );


        return result;
    }

}
