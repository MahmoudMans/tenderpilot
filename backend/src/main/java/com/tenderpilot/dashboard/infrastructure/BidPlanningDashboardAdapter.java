package com.tenderpilot.dashboard.infrastructure;

import com.tenderpilot.bidplanning.application.BidPlanningService;

import com.tenderpilot.bidplanning.api.BidPlanningWorkspaceResponse;

import com.tenderpilot.dashboard.application.DashboardBidOperationalGateway;

import com.tenderpilot.dashboard.domain.DashboardBidOperationalSnapshot;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class BidPlanningDashboardAdapter
        implements DashboardBidOperationalGateway {

    private final BidPlanningService
            bidPlanningService;


    public BidPlanningDashboardAdapter(
            BidPlanningService bidPlanningService
    ) {

        this.bidPlanningService =
                bidPlanningService;
    }


    @Override
    public DashboardBidOperationalSnapshot snapshot(
            UUID organizationId,
            UUID tenderId
    ) {

        BidPlanningWorkspaceResponse workspace =
                bidPlanningService
                        .workspace(
                                organizationId,
                                tenderId
                        );


        return new DashboardBidOperationalSnapshot(
                workspace.decisionOutcome() == null
                        ? null
                        : workspace
                                .decisionOutcome()
                                .name(),

                workspace.complianceReady(),

                workspace.complianceReadinessScore(),

                workspace
                        .statistics()
                        .openTasks(),

                workspace
                        .statistics()
                        .completedTasks(),

                workspace
                        .statistics()
                        .overdueTasks(),

                workspace
                        .statistics()
                        .blockedTasks(),

                workspace
                        .statistics()
                        .criticalOpenTasks(),

                workspace
                        .statistics()
                        .readyForSubmission(),

                workspace
                        .submissionControl()
                        .submittedAt()
                        !=
                        null
        );
    }

}
