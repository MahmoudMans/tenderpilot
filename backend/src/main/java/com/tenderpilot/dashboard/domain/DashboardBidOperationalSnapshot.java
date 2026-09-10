package com.tenderpilot.dashboard.domain;

public record DashboardBidOperationalSnapshot(

        String decisionOutcome,

        boolean complianceReady,

        int complianceReadinessScore,

        int openTasks,

        int completedTasks,

        int overdueTasks,

        int blockedTasks,

        int criticalOpenTasks,

        boolean readyForSubmission,

        boolean submitted

) {
}
