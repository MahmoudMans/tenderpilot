package com.tenderpilot.dashboard.application;

import com.tenderpilot.dashboard.api.DashboardCommandCenterResponse;

import com.tenderpilot.dashboard.domain.DashboardActionPriority;
import com.tenderpilot.dashboard.domain.DashboardBidOperationalSnapshot;
import com.tenderpilot.dashboard.domain.DashboardMarketSignal;
import com.tenderpilot.dashboard.domain.DashboardMatchObservation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class DashboardCommandCenterService {

    private final DashboardMatchGateway
            matchGateway;


    private final DashboardBidOperationalGateway
            operationalGateway;


    private final DashboardPublicMarketGateway
            publicMarketGateway;


    public DashboardCommandCenterService(
            DashboardMatchGateway matchGateway,
            DashboardBidOperationalGateway operationalGateway,
            DashboardPublicMarketGateway publicMarketGateway
    ) {

        this.matchGateway =
                matchGateway;

        this.operationalGateway =
                operationalGateway;

        this.publicMarketGateway =
                publicMarketGateway;
    }


    public DashboardCommandCenterResponse commandCenter(
            UUID organizationId
    ) {

        LocalDate today =
                LocalDate.now(
                        ZoneOffset.UTC
                );


        List<DashboardMatchObservation> matches =
                matchGateway
                        .matches(
                                organizationId
                        );


        List<DashboardCommandCenterResponse.BidCard> cards =
                matches
                        .stream()
                        .map(
                                match ->
                                        card(
                                                organizationId,
                                                match,
                                                today
                                        )
                        )
                        .sorted(
                                portfolioComparator()
                        )
                        .toList();


        DashboardMarketSignal market =
                marketSignal(
                        today
                );


        return new DashboardCommandCenterResponse(
                Instant.now(),

                summary(
                        cards
                ),

                new DashboardCommandCenterResponse
                        .MarketContext(
                                market.available(),
                                market.noticeCount(),
                                market.activeNoticeCount(),
                                market.uniqueBuyerCount(),
                                market.uniqueObservedSupplierCount(),
                                market.buyerConcentration(),
                                market.supplierConcentration(),
                                market.leadingCpv(),
                                market.leadingCpvSharePercentage(),
                                market.awardCoveragePercentage()
                        ),

                actions(
                        cards
                ),

                cards
        );
    }


    private DashboardCommandCenterResponse.BidCard card(
            UUID organizationId,
            DashboardMatchObservation match,
            LocalDate today
    ) {

        DashboardBidOperationalSnapshot operational =
                operationalGateway
                        .snapshot(
                                organizationId,
                                match.tenderId()
                        );


        Long daysToDeadline =
                match.submissionDeadline() == null
                        ? null
                        : ChronoUnit.DAYS.between(
                                today,

                                match
                                        .submissionDeadline()
                                        .atOffset(
                                                ZoneOffset.UTC
                                        )
                                        .toLocalDate()
                        );


        return new DashboardCommandCenterResponse
                .BidCard(
                        match.tenderId(),
                        match.buyerId(),
                        match.tenderTitle(),
                        match.buyerName(),
                        match.matchScore(),
                        match.matchGrade(),
                        match.blocked(),
                        normalizedOutcome(
                                operational.decisionOutcome()
                        ),
                        match.submissionDeadline(),
                        daysToDeadline,
                        operational.complianceReady(),
                        operational.complianceReadinessScore(),
                        operational.openTasks(),
                        operational.completedTasks(),
                        operational.overdueTasks(),
                        operational.blockedTasks(),
                        operational.criticalOpenTasks(),
                        operational.readyForSubmission(),
                        operational.submitted()
                );
    }


    private DashboardCommandCenterResponse.Summary summary(
            List<DashboardCommandCenterResponse.BidCard> cards
    ) {

        int highPotential =
                (int)
                        cards
                                .stream()
                                .filter(
                                        card ->
                                                !card.matchBlocked()
                                                &&
                                                card.matchScore()
                                                        >=
                                                        70
                                )
                                .count();


        int blockedMatches =
                (int)
                        cards
                                .stream()
                                .filter(
                                        DashboardCommandCenterResponse
                                                .BidCard::matchBlocked
                                )
                                .count();


        int go =
                countOutcome(
                        cards,
                        "GO"
                );


        int hold =
                countOutcome(
                        cards,
                        "HOLD"
                );


        int noGo =
                countOutcome(
                        cards,
                        "NO_GO"
                );


        int pending =
                (int)
                        cards
                                .stream()
                                .filter(
                                        card ->
                                                card.decisionOutcome() == null
                                                ||
                                                card.decisionOutcome()
                                                        .equals(
                                                                "PENDING"
                                                        )
                                )
                                .count();


        int complianceReady =
                (int)
                        cards
                                .stream()
                                .filter(
                                        DashboardCommandCenterResponse
                                                .BidCard::complianceReady
                                )
                                .count();


        int readyForSubmission =
                (int)
                        cards
                                .stream()
                                .filter(
                                        DashboardCommandCenterResponse
                                                .BidCard::readyForSubmission
                                )
                                .count();


        int submitted =
                (int)
                        cards
                                .stream()
                                .filter(
                                        DashboardCommandCenterResponse
                                                .BidCard::submitted
                                )
                                .count();


        int openTasks =
                cards
                        .stream()
                        .mapToInt(
                                DashboardCommandCenterResponse
                                        .BidCard::openTasks
                        )
                        .sum();


        int overdueTasks =
                cards
                        .stream()
                        .mapToInt(
                                DashboardCommandCenterResponse
                                        .BidCard::overdueTasks
                        )
                        .sum();


        int blockedTasks =
                cards
                        .stream()
                        .mapToInt(
                                DashboardCommandCenterResponse
                                        .BidCard::blockedTasks
                        )
                        .sum();


        int criticalTasks =
                cards
                        .stream()
                        .mapToInt(
                                DashboardCommandCenterResponse
                                        .BidCard::criticalOpenTasks
                        )
                        .sum();


        int deadlines7 =
                (int)
                        cards
                                .stream()
                                .filter(
                                        this::activeDeadline
                                )
                                .filter(
                                        card ->
                                                card.daysToDeadline()
                                                        <=
                                                        7
                                )
                                .count();


        int deadlines30 =
                (int)
                        cards
                                .stream()
                                .filter(
                                        this::activeDeadline
                                )
                                .filter(
                                        card ->
                                                card.daysToDeadline()
                                                        <=
                                                        30
                                )
                                .count();


        return new DashboardCommandCenterResponse
                .Summary(
                        cards.size(),
                        highPotential,
                        blockedMatches,
                        go,
                        hold,
                        noGo,
                        pending,
                        complianceReady,
                        readyForSubmission,
                        submitted,
                        openTasks,
                        overdueTasks,
                        blockedTasks,
                        criticalTasks,
                        deadlines7,
                        deadlines30
                );
    }


    private List<DashboardCommandCenterResponse.Action> actions(
            List<DashboardCommandCenterResponse.BidCard> cards
    ) {

        List<DashboardCommandCenterResponse.Action> result =
                new ArrayList<>();


        for (
                DashboardCommandCenterResponse.BidCard card :
                cards
        ) {

            DashboardCommandCenterResponse.Action action =
                    action(
                            card
                    );


            if (
                    action != null
            ) {

                result.add(
                        action
                );
            }
        }


        return result
                .stream()
                .sorted(
                        Comparator
                                .<DashboardCommandCenterResponse.Action>comparingInt(
                                        action ->
                                                priorityRank(
                                                        action.priority()
                                                )
                                )
                                .thenComparingLong(
                                        action ->
                                                deadlineRank(
                                                        action.tenderId(),
                                                        cards
                                                )
                                )
                )
                .limit(
                        12
                )
                .toList();
    }


    private DashboardCommandCenterResponse.Action action(
            DashboardCommandCenterResponse.BidCard card
    ) {

        if (
                card.submitted()
                ||
                "NO_GO".equals(
                        card.decisionOutcome()
                )
        ) {

            return null;
        }


        String workspace =
                "/bid-workspaces/"
                        +
                        card.tenderId();


        if (
                card.overdueTasks()
                        >
                        0
        ) {

            return new DashboardCommandCenterResponse
                    .Action(
                            DashboardActionPriority.CRITICAL,
                            "OVERDUE_WORK",
                            "Resolve overdue bid work",
                            card.overdueTasks()
                                    +
                                    " overdue task"
                                    +
                                    plural(
                                            card.overdueTasks()
                                    )
                                    +
                                    " on "
                                    +
                                    card.title(),
                            card.tenderId(),
                            workspace
                                    +
                                    "/planning"
                    );
        }


        if (
                card.daysToDeadline() != null
                &&
                card.daysToDeadline()
                        <
                        0
        ) {

            return new DashboardCommandCenterResponse
                    .Action(
                            DashboardActionPriority.CRITICAL,
                            "DEADLINE_PASSED",
                            "Review expired bid deadline",
                            "The recorded submission deadline has passed for "
                                    +
                                    card.title(),
                            card.tenderId(),
                            workspace
                    );
        }


        if (
                card.readyForSubmission()
        ) {

            return new DashboardCommandCenterResponse
                    .Action(
                            DashboardActionPriority.HIGH,
                            "READY_FOR_SUBMISSION",
                            "Complete final human submission",
                            card.title()
                                    +
                                    " has cleared the TenderPilot readiness gate.",
                            card.tenderId(),
                            workspace
                                    +
                                    "/planning"
                    );
        }


        if (
                card.daysToDeadline() != null
                &&
                card.daysToDeadline()
                        >=
                        0
                &&
                card.daysToDeadline()
                        <=
                        7
        ) {

            return new DashboardCommandCenterResponse
                    .Action(
                            DashboardActionPriority.HIGH,
                            "UPCOMING_DEADLINE",
                            "Deadline approaching",
                            card.title()
                                    +
                                    " is due in "
                                    +
                                    card.daysToDeadline()
                                    +
                                    " day"
                                    +
                                    plural(
                                            card.daysToDeadline()
                                    ),
                            card.tenderId(),
                            workspace
                    );
        }


        if (
                card.criticalOpenTasks()
                        >
                        0
        ) {

            return new DashboardCommandCenterResponse
                    .Action(
                            DashboardActionPriority.HIGH,
                            "CRITICAL_TASKS",
                            "Resolve critical bid tasks",
                            card.criticalOpenTasks()
                                    +
                                    " critical open task"
                                    +
                                    plural(
                                            card.criticalOpenTasks()
                                    )
                                    +
                                    " remain on "
                                    +
                                    card.title(),
                            card.tenderId(),
                            workspace
                                    +
                                    "/planning"
                    );
        }


        if (
                "GO".equals(
                        card.decisionOutcome()
                )
                &&
                !card.complianceReady()
        ) {

            return new DashboardCommandCenterResponse
                    .Action(
                            DashboardActionPriority.HIGH,
                            "COMPLIANCE_NOT_READY",
                            "Close compliance gaps",
                            card.title()
                                    +
                                    " is GO but compliance readiness is "
                                    +
                                    card.complianceReadinessScore()
                                    +
                                    "%.",
                            card.tenderId(),
                            workspace
                                    +
                                    "/compliance"
                    );
        }


        if (
                !card.matchBlocked()
                &&
                card.matchScore()
                        >=
                        70
                &&
                (
                        card.decisionOutcome() == null
                        ||
                        "PENDING".equals(
                                card.decisionOutcome()
                        )
                )
        ) {

            return new DashboardCommandCenterResponse
                    .Action(
                            DashboardActionPriority.MEDIUM,
                            "BID_DECISION_REQUIRED",
                            "Make a bid decision",
                            card.title()
                                    +
                                    " has a "
                                    +
                                    card.matchScore()
                                    +
                                    "% match score and remains undecided.",
                            card.tenderId(),
                            "/bid-decisions"
                    );
        }


        if (
                "GO".equals(
                        card.decisionOutcome()
                )
                &&
                card.complianceReady()
                &&
                card.openTasks()
                        >
                        0
        ) {

            return new DashboardCommandCenterResponse
                    .Action(
                            DashboardActionPriority.MEDIUM,
                            "OPEN_BID_WORK",
                            "Advance remaining bid work",
                            card.openTasks()
                                    +
                                    " open task"
                                    +
                                    plural(
                                            card.openTasks()
                                    )
                                    +
                                    " remain on "
                                    +
                                    card.title(),
                            card.tenderId(),
                            workspace
                                    +
                                    "/planning"
                    );
        }


        if (
                card.matchBlocked()
        ) {

            return new DashboardCommandCenterResponse
                    .Action(
                            DashboardActionPriority.LOW,
                            "BLOCKED_MATCH",
                            "Review blocked opportunity",
                            card.title()
                                    +
                                    " is blocked by the matching policy.",
                            card.tenderId(),
                            "/matches"
                    );
        }


        return null;
    }


    private DashboardMarketSignal marketSignal(
            LocalDate today
    ) {

        try {

            return publicMarketGateway
                    .snapshot(
                            today.minusDays(
                                    30
                            ),
                            today
                    );


        } catch (
                RuntimeException exception
        ) {

            return DashboardMarketSignal
                    .unavailable();
        }
    }


    private Comparator<DashboardCommandCenterResponse.BidCard>
    portfolioComparator() {

        return Comparator
                .<DashboardCommandCenterResponse.BidCard>comparingInt(
                        card ->
                                card.submitted()
                                        ? 1
                                        : 0
                )
                .thenComparingInt(
                        card ->
                                "NO_GO".equals(
                                        card.decisionOutcome()
                                )
                                        ? 1
                                        : 0
                )
                .thenComparing(
                        DashboardCommandCenterResponse
                                .BidCard::overdueTasks,

                        Comparator.reverseOrder()
                )
                .thenComparing(
                        card ->
                                card.daysToDeadline() == null
                                        ? Long.MAX_VALUE
                                        : card.daysToDeadline()
                )
                .thenComparing(
                        DashboardCommandCenterResponse
                                .BidCard::matchScore,

                        Comparator.reverseOrder()
                );
    }


    private int countOutcome(
            List<DashboardCommandCenterResponse.BidCard> cards,
            String outcome
    ) {

        return (int)
                cards
                        .stream()
                        .filter(
                                card ->
                                        outcome.equals(
                                                card.decisionOutcome()
                                        )
                        )
                        .count();
    }


    private boolean activeDeadline(
            DashboardCommandCenterResponse.BidCard card
    ) {

        return !card.submitted()
                &&
                !"NO_GO".equals(
                        card.decisionOutcome()
                )
                &&
                card.daysToDeadline() != null
                &&
                card.daysToDeadline()
                        >=
                        0;
    }


    private int priorityRank(
            DashboardActionPriority priority
    ) {

        return switch (
                priority
        ) {

            case CRITICAL ->
                    0;

            case HIGH ->
                    1;

            case MEDIUM ->
                    2;

            case LOW ->
                    3;
        };
    }


    private long deadlineRank(
            UUID tenderId,
            List<DashboardCommandCenterResponse.BidCard> cards
    ) {

        return cards
                .stream()
                .filter(
                        card ->
                                card.tenderId()
                                        .equals(
                                                tenderId
                                        )
                )
                .map(
                        DashboardCommandCenterResponse
                                .BidCard::daysToDeadline
                )
                .filter(
                        java.util.Objects::nonNull
                )
                .findFirst()
                .orElse(
                        Long.MAX_VALUE
                );
    }


    private String normalizedOutcome(
            String value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        String result =
                value
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        return result.isBlank()
                ? null
                : result;
    }


    private String plural(
            long count
    ) {

        return count == 1
                ? ""
                : "s";
    }

}
