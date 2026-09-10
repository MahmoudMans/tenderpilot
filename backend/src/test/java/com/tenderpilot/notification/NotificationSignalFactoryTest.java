package com.tenderpilot.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.dashboard.api.DashboardCommandCenterResponse;

import com.tenderpilot.dashboard.domain.DashboardActionPriority;

import com.tenderpilot.notification.application.NotificationSignalFactory;

import com.tenderpilot.notification.domain.NotificationSeverity;
import com.tenderpilot.notification.domain.NotificationType;
import com.tenderpilot.notification.domain.WatchlistItemSnapshot;
import com.tenderpilot.notification.domain.WatchlistTargetType;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class NotificationSignalFactoryTest {

    private static final UUID TENDER =
            UUID.fromString(
                    "21000000-0000-0000-0000-000000000001"
            );


    private static final UUID BUYER =
            UUID.fromString(
                    "21100000-0000-0000-0000-000000000001"
            );


    private final NotificationSignalFactory factory =
            new NotificationSignalFactory();


    @Test
    void dashboardPriorityBecomesPortfolioNotification() {

        var signals =
                factory.create(
                        dashboard(),
                        List.of()
                );


        assertThat(
                signals
        )
                .anySatisfy(
                        signal -> {

                            assertThat(
                                    signal.type()
                            )
                                    .isEqualTo(
                                            NotificationType.PORTFOLIO_ACTION
                                    );


                            assertThat(
                                    signal.severity()
                            )
                                    .isEqualTo(
                                            NotificationSeverity.HIGH
                                    );
                        }
                );
    }


    @Test
    void watchedTenderCreatesDeadlineAndHighMatchSignals() {

        var signals =
                factory.create(
                        dashboard(),

                        List.of(
                                new WatchlistItemSnapshot(
                                        UUID.randomUUID(),
                                        WatchlistTargetType.TENDER,
                                        TENDER.toString(),
                                        "Priority renovation"
                                )
                        )
                );


        assertThat(
                signals
                        .stream()
                        .map(
                                signal ->
                                        signal.type()
                        )
        )
                .contains(
                        NotificationType.WATCHLIST_DEADLINE,
                        NotificationType.WATCHLIST_HIGH_MATCH
                );
    }


    @Test
    void watchedBuyerCreatesPortfolioOpportunitySignal() {

        var signals =
                factory.create(
                        dashboard(),

                        List.of(
                                new WatchlistItemSnapshot(
                                        UUID.randomUUID(),
                                        WatchlistTargetType.BUYER,
                                        BUYER.toString(),
                                        "Strategic public buyer"
                                )
                        )
                );


        assertThat(
                signals
                        .stream()
                        .map(
                                signal ->
                                        signal.type()
                        )
        )
                .contains(
                        NotificationType.WATCHLIST_BUYER_OPPORTUNITY
                );
    }


    @Test
    void watchedCpvCreatesPublicMarketActivitySignal() {

        var signals =
                factory.create(
                        dashboard(),

                        List.of(
                                new WatchlistItemSnapshot(
                                        UUID.randomUUID(),
                                        WatchlistTargetType.CPV,
                                        "4545",
                                        "Refurbishment"
                                )
                        )
                );


        assertThat(
                signals
                        .stream()
                        .map(
                                signal ->
                                        signal.type()
                        )
        )
                .contains(
                        NotificationType.WATCHLIST_CPV_ACTIVITY
                );
    }


    @Test
    void unrelatedWatchTargetsDoNotCreateFalseSignals() {

        var signals =
                factory.create(
                        dashboard(),

                        List.of(
                                new WatchlistItemSnapshot(
                                        UUID.randomUUID(),
                                        WatchlistTargetType.TENDER,
                                        UUID.randomUUID().toString(),
                                        null
                                ),

                                new WatchlistItemSnapshot(
                                        UUID.randomUUID(),
                                        WatchlistTargetType.CPV,
                                        "9999",
                                        null
                                )
                        )
                );


        assertThat(
                signals
                        .stream()
                        .filter(
                                signal ->
                                        signal.type()
                                                !=
                                                NotificationType.PORTFOLIO_ACTION
                        )
        )
                .isEmpty();
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
                                100,
                                40,
                                20,
                                12,
                                "DIVERSIFIED",
                                "MODERATE",
                                "45453000",
                                38,
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
                                        "Strategic Public Buyer",
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

}
