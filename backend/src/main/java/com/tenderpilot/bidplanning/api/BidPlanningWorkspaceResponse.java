package com.tenderpilot.bidplanning.api;

import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.bidplanning.domain.BidPlanningEventType;
import com.tenderpilot.bidplanning.domain.BidTaskMilestone;
import com.tenderpilot.bidplanning.domain.BidTaskPriority;
import com.tenderpilot.bidplanning.domain.BidTaskStatus;

import java.time.Instant;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

public record BidPlanningWorkspaceResponse(

        UUID tenderId,

        BidDecisionOutcome decisionOutcome,

        boolean complianceReady,

        int complianceReadinessScore,

        boolean writable,

        String writeLockReason,

        Statistics statistics,

        SubmissionControl submissionControl,

        List<Task> tasks,

        List<Event> events

) {

    public record Statistics(

            int readinessScore,

            int totalTasks,

            int openTasks,

            int completedTasks,

            int blockedTasks,

            int overdueTasks,

            int criticalOpenTasks,

            int taskCompletionPercentage,

            int checklistCompletionPercentage,

            boolean readyForSubmission

    ) {
    }


    public record SubmissionControl(

            boolean portalReady,

            boolean signaturesReady,

            boolean technicalResponseReady,

            boolean commercialResponseReady,

            boolean administrativePackReady,

            boolean finalReviewReady,

            boolean submissionAuthorized,

            Instant plannedSubmissionAt,

            Instant submittedAt,

            String submissionReference,

            String submittedBy,

            String notes,

            String updatedBy

    ) {
    }


    public record Task(

            UUID id,

            UUID requirementId,

            String requirementTitle,

            String title,

            String description,

            BidTaskStatus status,

            BidTaskPriority priority,

            BidTaskMilestone milestone,

            String assignedTo,

            LocalDate dueDate,

            boolean overdue,

            Instant completedAt,

            String createdBy,

            String updatedBy,

            Instant createdAt,

            Instant updatedAt

    ) {
    }


    public record Event(

            UUID id,

            BidPlanningEventType eventType,

            UUID taskId,

            String actor,

            String detail,

            Instant createdAt

    ) {
    }

}
