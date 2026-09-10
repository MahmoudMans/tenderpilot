package com.tenderpilot.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.bidplanning.api.BidPlanningWorkspaceResponse;

import com.tenderpilot.bidplanning.application.BidPlanningService;

import com.tenderpilot.dashboard.infrastructure.BidPlanningDashboardAdapter;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class BidPlanningDashboardAdapterTest {

    private static final UUID ORG =
            UUID.fromString(
                    "20200000-0000-0000-0000-000000000001"
            );


    private static final UUID TENDER =
            UUID.fromString(
                    "20300000-0000-0000-0000-000000000001"
            );


    @Test
    void mapsPlanningWorkspaceIntoDashboardOperationalSnapshot() {

        BidPlanningService planningService =
                mock(
                        BidPlanningService.class
                );


        when(
                planningService.workspace(
                        ORG,
                        TENDER
                )
        )
                .thenReturn(
                        workspace(
                                BidDecisionOutcome.GO,
                                true,
                                88,
                                3,
                                4,
                                1,
                                1,
                                2,
                                false,
                                false
                        )
                );


        var adapter =
                new BidPlanningDashboardAdapter(
                        planningService
                );


        var snapshot =
                adapter.snapshot(
                        ORG,
                        TENDER
                );


        assertThat(
                snapshot.decisionOutcome()
        )
                .isEqualTo(
                        "GO"
                );


        assertThat(
                snapshot.complianceReadinessScore()
        )
                .isEqualTo(
                        88
                );


        assertThat(
                snapshot.openTasks()
        )
                .isEqualTo(
                        3
                );


        assertThat(
                snapshot.overdueTasks()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                snapshot.criticalOpenTasks()
        )
                .isEqualTo(
                        2
                );


        assertThat(
                snapshot.submitted()
        )
                .isFalse();
    }


    @Test
    void submittedTimestampMapsToFrozenSubmittedState() {

        BidPlanningService planningService =
                mock(
                        BidPlanningService.class
                );


        when(
                planningService.workspace(
                        ORG,
                        TENDER
                )
        )
                .thenReturn(
                        workspace(
                                BidDecisionOutcome.GO,
                                true,
                                100,
                                0,
                                6,
                                0,
                                0,
                                0,
                                true,
                                true
                        )
                );


        var adapter =
                new BidPlanningDashboardAdapter(
                        planningService
                );


        var snapshot =
                adapter.snapshot(
                        ORG,
                        TENDER
                );


        assertThat(
                snapshot.readyForSubmission()
        )
                .isTrue();


        assertThat(
                snapshot.submitted()
        )
                .isTrue();
    }


    private BidPlanningWorkspaceResponse workspace(
            BidDecisionOutcome decision,
            boolean complianceReady,
            int complianceScore,
            int openTasks,
            int completedTasks,
            int overdueTasks,
            int blockedTasks,
            int criticalTasks,
            boolean ready,
            boolean submitted
    ) {

        return new BidPlanningWorkspaceResponse(
                TENDER,

                decision,

                complianceReady,

                complianceScore,

                !submitted,

                submitted
                        ? "Submitted"
                        : null,

                new BidPlanningWorkspaceResponse
                        .Statistics(
                                ready
                                        ? 100
                                        : 65,

                                openTasks
                                        +
                                        completedTasks,

                                openTasks,

                                completedTasks,

                                blockedTasks,

                                overdueTasks,

                                criticalTasks,

                                openTasks
                                        +
                                        completedTasks
                                        ==
                                        0
                                        ? 100
                                        : (int)
                                                Math.round(
                                                        completedTasks
                                                                *
                                                                100.0
                                                                /
                                                                (
                                                                        openTasks
                                                                        +
                                                                        completedTasks
                                                                )
                                                ),

                                ready
                                        ? 100
                                        : 75,

                                ready
                        ),

                new BidPlanningWorkspaceResponse
                        .SubmissionControl(
                                ready,
                                ready,
                                ready,
                                ready,
                                ready,
                                ready,
                                ready,
                                null,
                                submitted
                                        ? Instant.parse(
                                                "2026-09-10T10:00:00Z"
                                        )
                                        : null,
                                submitted
                                        ? "TEST-RECEIPT"
                                        : null,
                                submitted
                                        ? "manager@example.test"
                                        : null,
                                null,
                                null
                        ),

                List.of(),

                List.of()
        );
    }

}
