package com.tenderpilot.bidplanning;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.bidplanning.application.BidSubmissionReadinessEngine;

import com.tenderpilot.bidplanning.domain.BidTaskPriority;
import com.tenderpilot.bidplanning.domain.BidTaskReadinessSnapshot;
import com.tenderpilot.bidplanning.domain.BidTaskStatus;
import com.tenderpilot.bidplanning.domain.SubmissionChecklistSnapshot;

import java.time.LocalDate;

import java.util.List;

import org.junit.jupiter.api.Test;

class BidSubmissionReadinessEngineTest {

    private final BidSubmissionReadinessEngine engine =
            new BidSubmissionReadinessEngine();


    @Test
    void fullGoComplianceTasksAndChecklistIsReady() {

        var result =
                engine.calculate(
                        BidDecisionOutcome.GO,

                        true,

                        List.of(
                                task(
                                        BidTaskStatus.DONE,
                                        BidTaskPriority.CRITICAL,
                                        LocalDate.of(
                                                2026,
                                                9,
                                                15
                                        )
                                )
                        ),

                        completeChecklist(),

                        LocalDate.of(
                                2026,
                                9,
                                10
                        )
                );


        assertThat(
                result.readinessScore()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                result.readyForSubmission()
        )
                .isTrue();
    }


    @Test
    void nonGoDecisionPreventsReadiness() {

        var result =
                engine.calculate(
                        BidDecisionOutcome.HOLD,
                        true,
                        List.of(),
                        completeChecklist(),
                        LocalDate.of(
                                2026,
                                9,
                                10
                        )
                );


        assertThat(
                result.readyForSubmission()
        )
                .isFalse();


        assertThat(
                result.readinessScore()
        )
                .isEqualTo(
                        80
                );
    }


    @Test
    void complianceMustBeReady() {

        var result =
                engine.calculate(
                        BidDecisionOutcome.GO,
                        false,
                        List.of(),
                        completeChecklist(),
                        LocalDate.of(
                                2026,
                                9,
                                10
                        )
                );


        assertThat(
                result.readyForSubmission()
        )
                .isFalse();


        assertThat(
                result.readinessScore()
        )
                .isEqualTo(
                        70
                );
    }


    @Test
    void openCriticalTaskBlocksSubmission() {

        var result =
                engine.calculate(
                        BidDecisionOutcome.GO,

                        true,

                        List.of(
                                task(
                                        BidTaskStatus.IN_PROGRESS,
                                        BidTaskPriority.CRITICAL,
                                        LocalDate.of(
                                                2026,
                                                9,
                                                20
                                        )
                                )
                        ),

                        completeChecklist(),

                        LocalDate.of(
                                2026,
                                9,
                                10
                        )
                );


        assertThat(
                result.criticalOpenTasks()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.openTasks()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.readyForSubmission()
        )
                .isFalse();
    }


    @Test
    void overdueTaskIsDetected() {

        var result =
                engine.calculate(
                        BidDecisionOutcome.GO,

                        true,

                        List.of(
                                task(
                                        BidTaskStatus.TODO,
                                        BidTaskPriority.HIGH,
                                        LocalDate.of(
                                                2026,
                                                9,
                                                1
                                        )
                                )
                        ),

                        completeChecklist(),

                        LocalDate.of(
                                2026,
                                9,
                                10
                        )
                );


        assertThat(
                result.overdueTasks()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.readyForSubmission()
        )
                .isFalse();
    }


    @Test
    void submissionAuthorizationIsRequired() {

        var checklist =
                new SubmissionChecklistSnapshot(
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        false
                );


        var result =
                engine.calculate(
                        BidDecisionOutcome.GO,
                        true,
                        List.of(),
                        checklist,
                        LocalDate.of(
                                2026,
                                9,
                                10
                        )
                );


        assertThat(
                result.checklistCompletionPercentage()
        )
                .isEqualTo(
                        86
                );


        assertThat(
                result.readyForSubmission()
        )
                .isFalse();
    }


    private BidTaskReadinessSnapshot task(
            BidTaskStatus status,
            BidTaskPriority priority,
            LocalDate dueDate
    ) {

        return new BidTaskReadinessSnapshot(
                status,
                priority,
                dueDate
        );
    }


    private SubmissionChecklistSnapshot completeChecklist() {

        return new SubmissionChecklistSnapshot(
                true,
                true,
                true,
                true,
                true,
                true,
                true
        );
    }

}
