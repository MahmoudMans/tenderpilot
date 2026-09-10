package com.tenderpilot.dashboard.api;

import com.tenderpilot.dashboard.domain.DashboardActionPriority;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

public record DashboardCommandCenterResponse(

        Instant generatedAt,

        Summary summary,

        MarketContext market,

        List<Action> priorities,

        List<BidCard> portfolio

) {

    public record Summary(

            int matchedOpportunities,

            int highPotentialMatches,

            int blockedMatches,

            int goBids,

            int holdBids,

            int noGoBids,

            int pendingDecisionBids,

            int complianceReadyBids,

            int submissionReadyBids,

            int submittedBids,

            int openTasks,

            int overdueTasks,

            int blockedTasks,

            int criticalOpenTasks,

            int deadlinesNext7Days,

            int deadlinesNext30Days

    ) {
    }


    public record MarketContext(

            boolean available,

            int noticeCount30Days,

            int activeNoticeCount30Days,

            int uniqueBuyerCount30Days,

            int uniqueObservedSupplierCount30Days,

            String buyerConcentration,

            String supplierConcentration,

            String leadingCpv,

            Integer leadingCpvSharePercentage,

            int awardCoveragePercentage30Days

    ) {
    }


    public record Action(

            DashboardActionPriority priority,

            String type,

            String title,

            String detail,

            UUID tenderId,

            String href

    ) {
    }


    public record BidCard(

            UUID tenderId,

            UUID buyerId,

            String title,

            String buyerName,

            int matchScore,

            String matchGrade,

            boolean matchBlocked,

            String decisionOutcome,

            Instant submissionDeadline,

            Long daysToDeadline,

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

}
