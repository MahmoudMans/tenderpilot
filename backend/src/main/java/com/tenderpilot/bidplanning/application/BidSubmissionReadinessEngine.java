package com.tenderpilot.bidplanning.application;

import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.bidplanning.domain.BidSubmissionReadiness;
import com.tenderpilot.bidplanning.domain.BidTaskPriority;
import com.tenderpilot.bidplanning.domain.BidTaskReadinessSnapshot;
import com.tenderpilot.bidplanning.domain.BidTaskStatus;
import com.tenderpilot.bidplanning.domain.SubmissionChecklistSnapshot;

import java.time.LocalDate;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class BidSubmissionReadinessEngine {

    public BidSubmissionReadiness calculate(
            BidDecisionOutcome decisionOutcome,
            boolean complianceReady,
            List<BidTaskReadinessSnapshot> tasks,
            SubmissionChecklistSnapshot checklist,
            LocalDate today
    ) {

        List<BidTaskReadinessSnapshot> safeTasks =
                tasks == null
                        ? List.of()
                        : tasks;


        SubmissionChecklistSnapshot safeChecklist =
                checklist == null
                        ? new SubmissionChecklistSnapshot(
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false
                        )
                        : checklist;


        LocalDate effectiveToday =
                today == null
                        ? LocalDate.now()
                        : today;


        int totalTasks =
                safeTasks.size();


        int completedTasks =
                (int)
                        safeTasks
                                .stream()
                                .filter(
                                        this::closed
                                )
                                .count();


        int openTasks =
                totalTasks
                        -
                        completedTasks;


        int blockedTasks =
                (int)
                        safeTasks
                                .stream()
                                .filter(
                                        task ->
                                                task.status()
                                                        ==
                                                        BidTaskStatus.BLOCKED
                                )
                                .count();


        int overdueTasks =
                (int)
                        safeTasks
                                .stream()
                                .filter(
                                        task ->
                                                !closed(
                                                        task
                                                )
                                                &&
                                                task.dueDate()
                                                        !=
                                                        null
                                                &&
                                                task.dueDate()
                                                        .isBefore(
                                                                effectiveToday
                                                        )
                                )
                                .count();


        int criticalOpenTasks =
                (int)
                        safeTasks
                                .stream()
                                .filter(
                                        task ->
                                                !closed(
                                                        task
                                                )
                                                &&
                                                task.priority()
                                                        ==
                                                        BidTaskPriority.CRITICAL
                                )
                                .count();


        int taskCompletion =
                totalTasks == 0
                        ? 100
                        : (int)
                                Math.round(
                                        completedTasks
                                                *
                                                100.0
                                                /
                                                totalTasks
                                );


        int checklistCompletion =
                (int)
                        Math.round(
                                safeChecklist
                                        .completedCount()
                                        *
                                        100.0
                                        /
                                        7.0
                        );


        int score =
                0;


        if (
                decisionOutcome
                        ==
                        BidDecisionOutcome.GO
        ) {

            score +=
                    20;
        }


        if (
                complianceReady
        ) {

            score +=
                    30;
        }


        score +=
                (int)
                        Math.round(
                                taskCompletion
                                        *
                                        0.20
                        );


        score +=
                (int)
                        Math.round(
                                checklistCompletion
                                        *
                                        0.30
                        );


        score =
                Math.max(
                        0,
                        Math.min(
                                100,
                                score
                        )
                );


        boolean ready =
                decisionOutcome
                        ==
                        BidDecisionOutcome.GO
                &&
                complianceReady
                &&
                openTasks == 0
                &&
                blockedTasks == 0
                &&
                overdueTasks == 0
                &&
                criticalOpenTasks == 0
                &&
                safeChecklist.complete();


        return new BidSubmissionReadiness(
                score,
                totalTasks,
                openTasks,
                completedTasks,
                blockedTasks,
                overdueTasks,
                criticalOpenTasks,
                taskCompletion,
                checklistCompletion,
                ready
        );
    }


    private boolean closed(
            BidTaskReadinessSnapshot task
    ) {

        return task.status()
                ==
                BidTaskStatus.DONE
                ||
                task.status()
                        ==
                        BidTaskStatus.CANCELLED;
    }

}
