package com.tenderpilot.notification;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.tenderpilot.dashboard.api.DashboardCommandCenterResponse;

import com.tenderpilot.dashboard.application.DashboardCommandCenterService;

import com.tenderpilot.dashboard.domain.DashboardActionPriority;

import com.tenderpilot.notification.api.CreateWatchlistItemRequest;

import com.tenderpilot.notification.application.NotificationCenterService;

import com.tenderpilot.notification.domain.NotificationStatus;
import com.tenderpilot.notification.domain.NotificationType;
import com.tenderpilot.notification.domain.WatchlistTargetType;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_notifications;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=false",
                "tenderpilot.search.auto-index=false",
                "tenderpilot.local-ai.enabled=false",
                "tenderpilot.rag.enabled=false"
        }
)
@ActiveProfiles(
        "test"
)
@Import(
        NotificationCenterIntegrationTest
                .FakeDashboardConfiguration.class
)
@Transactional
class NotificationCenterIntegrationTest {

    private static final UUID ORG_A =
            UUID.fromString(
                    "21200000-0000-0000-0000-000000000001"
            );


    private static final UUID ORG_B =
            UUID.fromString(
                    "21200000-0000-0000-0000-000000000002"
            );


    private static final UUID TENDER =
            UUID.fromString(
                    "21300000-0000-0000-0000-000000000001"
            );


    private static final UUID BUYER =
            UUID.fromString(
                    "21400000-0000-0000-0000-000000000001"
            );


    @Autowired
    private NotificationCenterService
            notificationCenterService;


    @Autowired
    private DashboardCommandCenterService
            dashboardService;


    @BeforeEach
    void configureDashboard() {

        reset(
                dashboardService
        );


        when(
                dashboardService.commandCenter(
                        any(
                                UUID.class
                        )
                )
        )
                .thenReturn(
                        dashboard()
                );
    }


    @Test
    void watchlistCreationIsNormalizedAndIdempotent() {

        var first =
                notificationCenterService
                        .createWatchlistItem(
                                ORG_A,

                                new CreateWatchlistItemRequest(
                                        WatchlistTargetType.CPV,
                                        " 4545 ",
                                        " Renovation "
                                )
                        );


        assertThat(
                first.watchlist()
        )
                .hasSize(
                        1
                );


        assertThat(
                first
                        .watchlist()
                        .getFirst()
                        .targetKey()
        )
                .isEqualTo(
                        "4545"
                );


        var second =
                notificationCenterService
                        .createWatchlistItem(
                                ORG_A,

                                new CreateWatchlistItemRequest(
                                        WatchlistTargetType.CPV,
                                        "4545",
                                        "Updated renovation"
                                )
                        );


        assertThat(
                second.watchlist()
        )
                .hasSize(
                        1
                );


        assertThat(
                second
                        .watchlist()
                        .getFirst()
                        .label()
        )
                .isEqualTo(
                        "Updated renovation"
                );
    }


    @Test
    void refreshPersistsPortfolioAndWatchlistSignals() {

        notificationCenterService
                .createWatchlistItem(
                        ORG_A,

                        new CreateWatchlistItemRequest(
                                WatchlistTargetType.TENDER,
                                TENDER.toString(),
                                "Priority renovation"
                        )
                );


        var center =
                notificationCenterService
                        .refresh(
                                ORG_A
                        );


        assertThat(
                center.summary()
                        .activeCount()
        )
                .isGreaterThanOrEqualTo(
                        3
                );


        assertThat(
                center
                        .notifications()
                        .stream()
                        .map(
                                item ->
                                        item.type()
                        )
        )
                .contains(
                        NotificationType.PORTFOLIO_ACTION,
                        NotificationType.WATCHLIST_DEADLINE,
                        NotificationType.WATCHLIST_HIGH_MATCH
                );


        assertThat(
                center.summary()
                        .unreadCount()
        )
                .isGreaterThan(
                        0
                );
    }


    @Test
    void notificationCanBeAcknowledgedAndDismissed() {

        var refreshed =
                notificationCenterService
                        .refresh(
                                ORG_A
                        );


        UUID notificationId =
                refreshed
                        .notifications()
                        .stream()
                        .filter(
                                item ->
                                        item.active()
                        )
                        .findFirst()
                        .orElseThrow()
                        .id();


        var acknowledged =
                notificationCenterService
                        .acknowledge(
                                ORG_A,
                                notificationId
                        );


        assertThat(
                acknowledged
                        .notifications()
                        .stream()
                        .filter(
                                item ->
                                        item.id()
                                                .equals(
                                                        notificationId
                                                )
                        )
                        .findFirst()
                        .orElseThrow()
                        .status()
        )
                .isEqualTo(
                        NotificationStatus.READ
                );


        var dismissed =
                notificationCenterService
                        .dismiss(
                                ORG_A,
                                notificationId
                        );


        var dismissedItem =
                dismissed
                        .notifications()
                        .stream()
                        .filter(
                                item ->
                                        item.id()
                                                .equals(
                                                        notificationId
                                                )
                        )
                        .findFirst()
                        .orElseThrow();


        assertThat(
                dismissedItem.status()
        )
                .isEqualTo(
                        NotificationStatus.DISMISSED
                );


        assertThat(
                dismissedItem.active()
        )
                .isFalse();
    }


    @Test
    void watchlistsAreTenantIsolated() {

        notificationCenterService
                .createWatchlistItem(
                        ORG_A,

                        new CreateWatchlistItemRequest(
                                WatchlistTargetType.TENDER,
                                TENDER.toString(),
                                "Org A"
                        )
                );


        notificationCenterService
                .createWatchlistItem(
                        ORG_B,

                        new CreateWatchlistItemRequest(
                                WatchlistTargetType.BUYER,
                                BUYER.toString(),
                                "Org B"
                        )
                );


        var centerA =
                notificationCenterService
                        .center(
                                ORG_A
                        );


        var centerB =
                notificationCenterService
                        .center(
                                ORG_B
                        );


        assertThat(
                centerA.watchlist()
        )
                .hasSize(
                        1
                );


        assertThat(
                centerB.watchlist()
        )
                .hasSize(
                        1
                );


        assertThat(
                centerA
                        .watchlist()
                        .getFirst()
                        .type()
        )
                .isEqualTo(
                        WatchlistTargetType.TENDER
                );


        assertThat(
                centerB
                        .watchlist()
                        .getFirst()
                        .type()
        )
                .isEqualTo(
                        WatchlistTargetType.BUYER
                );
    }


    @Test
    void markAllReadClearsActiveUnreadCount() {

        var refreshed =
                notificationCenterService
                        .refresh(
                                ORG_A
                        );


        assertThat(
                refreshed.summary()
                        .unreadCount()
        )
                .isGreaterThan(
                        0
                );


        var read =
                notificationCenterService
                        .markAllRead(
                                ORG_A
                        );


        assertThat(
                read.summary()
                        .unreadCount()
        )
                .isZero();
    }


    private DashboardCommandCenterResponse dashboard() {

        return new DashboardCommandCenterResponse(
                Instant.parse(
                        "2026-09-10T10:00:00Z"
                ),

                new DashboardCommandCenterResponse
                        .Summary(
                                1,
                                1,
                                0,
                                1,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                2,
                                0,
                                0,
                                0,
                                1,
                                1
                        ),

                new DashboardCommandCenterResponse
                        .MarketContext(
                                true,
                                125,
                                40,
                                18,
                                12,
                                "DIVERSIFIED",
                                "MODERATE",
                                "45453000",
                                40,
                                50
                        ),

                List.of(
                        new DashboardCommandCenterResponse
                                .Action(
                                        DashboardActionPriority.HIGH,
                                        "UPCOMING_DEADLINE",
                                        "Deadline approaching",
                                        "Priority renovation is due soon.",
                                        TENDER,
                                        "/bid-workspaces/"
                                                +
                                                TENDER
                                )
                ),

                List.of(
                        new DashboardCommandCenterResponse
                                .BidCard(
                                        TENDER,
                                        BUYER,
                                        "Priority renovation",
                                        "Strategic Buyer",
                                        91,
                                        "EXCELLENT",
                                        false,
                                        "GO",
                                        Instant.parse(
                                                "2026-09-13T12:00:00Z"
                                        ),
                                        3L,
                                        false,
                                        70,
                                        2,
                                        1,
                                        0,
                                        0,
                                        0,
                                        false,
                                        false
                                )
                )
        );
    }


    @TestConfiguration
    static class FakeDashboardConfiguration {

        @Bean
        @Primary
        DashboardCommandCenterService dashboardCommandCenterService() {

            return mock(
                    DashboardCommandCenterService.class
            );
        }

    }

}
