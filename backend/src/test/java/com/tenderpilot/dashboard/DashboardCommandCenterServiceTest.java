package com.tenderpilot.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.dashboard.application.DashboardBidOperationalGateway;
import com.tenderpilot.dashboard.application.DashboardCommandCenterService;
import com.tenderpilot.dashboard.application.DashboardMatchGateway;
import com.tenderpilot.dashboard.application.DashboardPublicMarketGateway;

import com.tenderpilot.dashboard.domain.DashboardActionPriority;
import com.tenderpilot.dashboard.domain.DashboardBidOperationalSnapshot;
import com.tenderpilot.dashboard.domain.DashboardMarketSignal;
import com.tenderpilot.dashboard.domain.DashboardMatchObservation;

import java.time.LocalDate;
import java.time.ZoneOffset;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class DashboardCommandCenterServiceTest {

    private static final UUID ORG =
            UUID.fromString(
                    "20000000-0000-0000-0000-000000000001"
            );


    private static final UUID TENDER_ONE =
            UUID.fromString(
                    "20100000-0000-0000-0000-000000000001"
            );


    private static final UUID TENDER_TWO =
            UUID.fromString(
                    "20100000-0000-0000-0000-000000000002"
            );


    private static final UUID TENDER_THREE =
            UUID.fromString(
                    "20100000-0000-0000-0000-000000000003"
            );


    @Test
    void commandCenterAggregatesPortfolioAndOperationalMetrics() {

        var service =
                service(
                        List.of(
                                match(
                                        TENDER_ONE,
                                        "Strong GO bid",
                                        90,
                                        20
                                ),

                                match(
                                        TENDER_TWO,
                                        "Pending opportunity",
                                        78,
                                        15
                                ),

                                match(
                                        TENDER_THREE,
                                        "No go opportunity",
                                        50,
                                        40
                                )
                        ),

                        Map.of(
                                TENDER_ONE,
                                operational(
                                        "GO",
                                        true,
                                        90,
                                        2,
                                        1,
                                        0,
                                        0,
                                        0,
                                        false,
                                        false
                                ),

                                TENDER_TWO,
                                operational(
                                        "PENDING",
                                        false,
                                        40,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        false,
                                        false
                                ),

                                TENDER_THREE,
                                operational(
                                        "NO_GO",
                                        false,
                                        10,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        false,
                                        false
                                )
                        )
                );


        var result =
                service.commandCenter(
                        ORG
                );


        assertThat(
                result.summary()
                        .matchedOpportunities()
        )
                .isEqualTo(
                        3
                );


        assertThat(
                result.summary()
                        .highPotentialMatches()
        )
                .isEqualTo(
                        2
                );


        assertThat(
                result.summary()
                        .goBids()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.summary()
                        .pendingDecisionBids()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.summary()
                        .noGoBids()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.summary()
                        .openTasks()
        )
                .isEqualTo(
                        2
                );
    }


    @Test
    void overdueWorkBecomesCriticalPriority() {

        var service =
                service(
                        List.of(
                                match(
                                        TENDER_ONE,
                                        "Urgent bid",
                                        88,
                                        14
                                )
                        ),

                        Map.of(
                                TENDER_ONE,
                                operational(
                                        "GO",
                                        false,
                                        60,
                                        4,
                                        0,
                                        2,
                                        1,
                                        1,
                                        false,
                                        false
                                )
                        )
                );


        var result =
                service.commandCenter(
                        ORG
                );


        assertThat(
                result.priorities()
        )
                .hasSize(
                        1
                );


        assertThat(
                result
                        .priorities()
                        .getFirst()
                        .priority()
        )
                .isEqualTo(
                        DashboardActionPriority.CRITICAL
                );


        assertThat(
                result
                        .priorities()
                        .getFirst()
                        .type()
        )
                .isEqualTo(
                        "OVERDUE_WORK"
                );
    }


    @Test
    void strongPendingMatchCreatesDecisionPriority() {

        var service =
                service(
                        List.of(
                                match(
                                        TENDER_TWO,
                                        "Strategic opportunity",
                                        85,
                                        25
                                )
                        ),

                        Map.of(
                                TENDER_TWO,
                                operational(
                                        "PENDING",
                                        false,
                                        20,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        false,
                                        false
                                )
                        )
                );


        var result =
                service.commandCenter(
                        ORG
                );


        assertThat(
                result
                        .priorities()
                        .getFirst()
                        .type()
        )
                .isEqualTo(
                        "BID_DECISION_REQUIRED"
                );


        assertThat(
                result
                        .priorities()
                        .getFirst()
                        .priority()
        )
                .isEqualTo(
                        DashboardActionPriority.MEDIUM
                );
    }


    @Test
    void submissionReadyBidCreatesFinalHumanSubmissionPriority() {

        var service =
                service(
                        List.of(
                                match(
                                        TENDER_ONE,
                                        "Submission ready",
                                        95,
                                        12
                                )
                        ),

                        Map.of(
                                TENDER_ONE,
                                operational(
                                        "GO",
                                        true,
                                        100,
                                        0,
                                        5,
                                        0,
                                        0,
                                        0,
                                        true,
                                        false
                                )
                        )
                );


        var result =
                service.commandCenter(
                        ORG
                );


        assertThat(
                result.summary()
                        .submissionReadyBids()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result
                        .priorities()
                        .getFirst()
                        .type()
        )
                .isEqualTo(
                        "READY_FOR_SUBMISSION"
                );
    }


    @Test
    void publicMarketContextIsKeptSeparateFromPrivatePortfolio() {

        var service =
                service(
                        List.of(),
                        Map.of()
                );


        var result =
                service.commandCenter(
                        ORG
                );


        assertThat(
                result.market()
                        .available()
        )
                .isTrue();


        assertThat(
                result.market()
                        .noticeCount30Days()
        )
                .isEqualTo(
                        125
                );


        assertThat(
                result.market()
                        .leadingCpv()
        )
                .isEqualTo(
                        "45453000"
                );


        assertThat(
                result.summary()
                        .matchedOpportunities()
        )
                .isZero();
    }


    private DashboardCommandCenterService service(
            List<DashboardMatchObservation> matches,
            Map<UUID, DashboardBidOperationalSnapshot> operations
    ) {

        DashboardMatchGateway matchGateway =
                organizationId ->
                        matches;


        DashboardBidOperationalGateway operationalGateway =
                (
                        organizationId,
                        tenderId
                ) -> {

                    DashboardBidOperationalSnapshot snapshot =
                            operations.get(
                                    tenderId
                            );


                    if (
                            snapshot == null
                    ) {

                        return operational(
                                null,
                                false,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                false,
                                false
                        );
                    }


                    return snapshot;
                };


        DashboardPublicMarketGateway marketGateway =
                (
                        dateFrom,
                        dateTo
                ) ->
                        new DashboardMarketSignal(
                                true,
                                125,
                                40,
                                18,
                                12,
                                "DIVERSIFIED",
                                "MODERATE",
                                "45453000",
                                34,
                                52
                        );


        return new DashboardCommandCenterService(
                matchGateway,
                operationalGateway,
                marketGateway
        );
    }


    private DashboardMatchObservation match(
            UUID tenderId,
            String title,
            int score,
            long daysToDeadline
    ) {

        LocalDate date =
                LocalDate.now(
                        ZoneOffset.UTC
                )
                        .plusDays(
                                daysToDeadline
                        );


        return new DashboardMatchObservation(
                tenderId,
                UUID.randomUUID(),
                title,
                "Public Buyer",
                date
                        .atStartOfDay()
                        .toInstant(
                                ZoneOffset.UTC
                        ),
                score,
                score >= 85
                        ? "EXCELLENT"
                        : score >= 70
                                ? "STRONG"
                                : "WEAK",
                false
        );
    }


    private DashboardBidOperationalSnapshot operational(
            String outcome,
            boolean complianceReady,
            int complianceScore,
            int openTasks,
            int completedTasks,
            int overdueTasks,
            int blockedTasks,
            int criticalOpenTasks,
            boolean submissionReady,
            boolean submitted
    ) {

        return new DashboardBidOperationalSnapshot(
                outcome,
                complianceReady,
                complianceScore,
                openTasks,
                completedTasks,
                overdueTasks,
                blockedTasks,
                criticalOpenTasks,
                submissionReady,
                submitted
        );
    }

}
