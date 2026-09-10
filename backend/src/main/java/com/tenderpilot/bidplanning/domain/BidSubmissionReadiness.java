package com.tenderpilot.bidplanning.domain;

public record BidSubmissionReadiness(

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
