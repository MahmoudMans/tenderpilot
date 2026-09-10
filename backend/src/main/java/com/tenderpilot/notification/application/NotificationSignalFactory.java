package com.tenderpilot.notification.application;

import com.tenderpilot.dashboard.api.DashboardCommandCenterResponse;

import com.tenderpilot.dashboard.domain.DashboardActionPriority;

import com.tenderpilot.notification.domain.AlertSignal;
import com.tenderpilot.notification.domain.NotificationSeverity;
import com.tenderpilot.notification.domain.NotificationType;
import com.tenderpilot.notification.domain.WatchlistItemSnapshot;
import com.tenderpilot.notification.domain.WatchlistTargetType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class NotificationSignalFactory {

    public List<AlertSignal> create(
            DashboardCommandCenterResponse dashboard,
            List<WatchlistItemSnapshot> watchlist
    ) {

        Map<String, AlertSignal> signals =
                new LinkedHashMap<>();


        addDashboardActions(
                dashboard,
                signals
        );


        addWatchlistSignals(
                dashboard,
                watchlist == null
                        ? List.of()
                        : watchlist,
                signals
        );


        return signals
                .values()
                .stream()
                .sorted(
                        Comparator
                                .<AlertSignal>comparingInt(
                                        signal ->
                                                severityRank(
                                                        signal.severity()
                                                )
                                )
                                .thenComparing(
                                        AlertSignal::fingerprint
                                )
                )
                .toList();
    }


    private void addDashboardActions(
            DashboardCommandCenterResponse dashboard,
            Map<String, AlertSignal> signals
    ) {

        if (
                dashboard == null
                ||
                dashboard.priorities() == null
        ) {

            return;
        }


        for (
                DashboardCommandCenterResponse.Action action :
                dashboard.priorities()
        ) {

            String fingerprint =
                    "dashboard:"
                            +
                            action.type()
                            +
                            ":"
                            +
                            (
                                    action.tenderId() == null
                                            ? "global"
                                            : action.tenderId()
                            );


            signals.put(
                    fingerprint,

                    new AlertSignal(
                            fingerprint,
                            NotificationType.PORTFOLIO_ACTION,
                            severity(
                                    action.priority()
                            ),
                            action.title(),
                            action.detail(),
                            action.tenderId(),
                            action.href()
                    )
            );
        }
    }


    private void addWatchlistSignals(
            DashboardCommandCenterResponse dashboard,
            List<WatchlistItemSnapshot> watchlist,
            Map<String, AlertSignal> signals
    ) {

        if (
                dashboard == null
        ) {

            return;
        }


        Map<UUID, DashboardCommandCenterResponse.BidCard>
                portfolioByTender =
                new LinkedHashMap<>();


        if (
                dashboard.portfolio() != null
        ) {

            for (
                    DashboardCommandCenterResponse.BidCard card :
                    dashboard.portfolio()
            ) {

                portfolioByTender.put(
                        card.tenderId(),
                        card
                );
            }
        }


        for (
                WatchlistItemSnapshot item :
                watchlist
        ) {

            if (
                    item.type() == WatchlistTargetType.TENDER
            ) {

                tenderSignals(
                        item,
                        portfolioByTender,
                        signals
                );


            } else if (
                    item.type() == WatchlistTargetType.BUYER
            ) {

                buyerSignals(
                        item,
                        dashboard,
                        signals
                );


            } else if (
                    item.type() == WatchlistTargetType.CPV
            ) {

                cpvSignals(
                        item,
                        dashboard,
                        signals
                );
            }
        }
    }


    private void tenderSignals(
            WatchlistItemSnapshot item,
            Map<UUID, DashboardCommandCenterResponse.BidCard>
                    portfolioByTender,
            Map<String, AlertSignal> signals
    ) {

        UUID tenderId;


        try {

            tenderId =
                    UUID.fromString(
                            item.targetKey()
                    );


        } catch (
                IllegalArgumentException exception
        ) {

            return;
        }


        DashboardCommandCenterResponse.BidCard card =
                portfolioByTender.get(
                        tenderId
                );


        if (
                card == null
        ) {

            return;
        }


        String name =
                label(
                        item,
                        card.title()
                );


        if (
                card.overdueTasks()
                        >
                        0
        ) {

            add(
                    signals,

                    signal(
                            "watch:tender:overdue:"
                                    +
                                    tenderId,

                            NotificationType.WATCHLIST_OVERDUE,

                            NotificationSeverity.CRITICAL,

                            "Watched bid has overdue work",

                            name
                                    +
                                    " has "
                                    +
                                    card.overdueTasks()
                                    +
                                    " overdue task"
                                    +
                                    plural(
                                            card.overdueTasks()
                                    )
                                    +
                                    ".",

                            tenderId,

                            "/bid-workspaces/"
                                    +
                                    tenderId
                                    +
                                    "/planning"
                    )
            );
        }


        if (
                !card.submitted()
                &&
                !"NO_GO".equals(
                        card.decisionOutcome()
                )
                &&
                card.daysToDeadline() != null
                &&
                card.daysToDeadline() >= 0
                &&
                card.daysToDeadline() <= 7
        ) {

            add(
                    signals,

                    signal(
                            "watch:tender:deadline:"
                                    +
                                    tenderId,

                            NotificationType.WATCHLIST_DEADLINE,

                            NotificationSeverity.HIGH,

                            "Watched tender deadline approaching",

                            name
                                    +
                                    " is due in "
                                    +
                                    card.daysToDeadline()
                                    +
                                    " day"
                                    +
                                    plural(
                                            card.daysToDeadline()
                                    )
                                    +
                                    ".",

                            tenderId,

                            "/bid-workspaces/"
                                    +
                                    tenderId
                    )
            );
        }


        if (
                !card.submitted()
                &&
                card.readyForSubmission()
        ) {

            add(
                    signals,

                    signal(
                            "watch:tender:submission-ready:"
                                    +
                                    tenderId,

                            NotificationType.WATCHLIST_SUBMISSION_READY,

                            NotificationSeverity.HIGH,

                            "Watched bid is submission ready",

                            name
                                    +
                                    " has cleared the TenderPilot submission-readiness gate.",

                            tenderId,

                            "/bid-workspaces/"
                                    +
                                    tenderId
                                    +
                                    "/planning"
                    )
            );
        }


        if (
                "GO".equals(
                        card.decisionOutcome()
                )
                &&
                !card.complianceReady()
        ) {

            add(
                    signals,

                    signal(
                            "watch:tender:compliance:"
                                    +
                                    tenderId,

                            NotificationType.WATCHLIST_COMPLIANCE,

                            NotificationSeverity.HIGH,

                            "Watched GO bid has compliance gaps",

                            name
                                    +
                                    " is GO with compliance readiness at "
                                    +
                                    card.complianceReadinessScore()
                                    +
                                    "%.",

                            tenderId,

                            "/bid-workspaces/"
                                    +
                                    tenderId
                                    +
                                    "/compliance"
                    )
            );
        }


        if (
                !card.matchBlocked()
                &&
                card.matchScore()
                        >=
                        85
        ) {

            add(
                    signals,

                    signal(
                            "watch:tender:high-match:"
                                    +
                                    tenderId,

                            NotificationType.WATCHLIST_HIGH_MATCH,

                            NotificationSeverity.MEDIUM,

                            "Watched tender is a high match",

                            name
                                    +
                                    " currently has a "
                                    +
                                    card.matchScore()
                                    +
                                    "% match score.",

                            tenderId,

                            "/opportunities/"
                                    +
                                    tenderId
                    )
            );
        }
    }


    private void buyerSignals(
            WatchlistItemSnapshot item,
            DashboardCommandCenterResponse dashboard,
            Map<String, AlertSignal> signals
    ) {

        UUID buyerId;


        try {

            buyerId =
                    UUID.fromString(
                            item.targetKey()
                    );


        } catch (
                IllegalArgumentException exception
        ) {

            return;
        }


        List<DashboardCommandCenterResponse.BidCard> cards =
                dashboard
                        .portfolio()
                        .stream()
                        .filter(
                                card ->
                                        buyerId.equals(
                                                card.buyerId()
                                        )
                                &&
                                !card.submitted()
                                &&
                                !"NO_GO".equals(
                                        card.decisionOutcome()
                                )
                        )
                        .toList();


        if (
                cards.isEmpty()
        ) {

            return;
        }


        DashboardCommandCenterResponse.BidCard strongest =
                cards
                        .stream()
                        .max(
                                Comparator.comparingInt(
                                        DashboardCommandCenterResponse
                                                .BidCard::matchScore
                                )
                        )
                        .orElseThrow();


        String name =
                label(
                        item,
                        strongest.buyerName()
                );


        add(
                signals,

                signal(
                        "watch:buyer:portfolio:"
                                +
                                buyerId,

                        NotificationType.WATCHLIST_BUYER_OPPORTUNITY,

                        strongest.matchScore() >= 85
                                ? NotificationSeverity.HIGH
                                : NotificationSeverity.MEDIUM,

                        "Watched buyer has active opportunities",

                        name
                                +
                                " currently appears on "
                                +
                                cards.size()
                                +
                                " active matched opportunit"
                                +
                                (
                                        cards.size() == 1
                                                ? "y"
                                                : "ies"
                                )
                                +
                                "; strongest match is "
                                +
                                strongest.matchScore()
                                +
                                "%.",

                        strongest.tenderId(),

                        "/buyers/"
                                +
                                buyerId
                )
        );
    }


    private void cpvSignals(
            WatchlistItemSnapshot item,
            DashboardCommandCenterResponse dashboard,
            Map<String, AlertSignal> signals
    ) {

        if (
                dashboard.market() == null
                ||
                !dashboard.market().available()
                ||
                dashboard.market().leadingCpv() == null
        ) {

            return;
        }


        if (
                !dashboard
                        .market()
                        .leadingCpv()
                        .startsWith(
                                item.targetKey()
                        )
        ) {

            return;
        }


        String name =
                item.label() == null
                        ||
                        item.label().isBlank()
                        ? "CPV "
                                +
                                item.targetKey()
                        : item.label();


        add(
                signals,

                signal(
                        "watch:cpv:leading:"
                                +
                                item.targetKey(),

                        NotificationType.WATCHLIST_CPV_ACTIVITY,

                        NotificationSeverity.MEDIUM,

                        "Watched CPV leads recent market activity",

                        name
                                +
                                " matches the leading CPV in the current 30-day public market pulse"
                                +
                                (
                                        dashboard
                                                .market()
                                                .leadingCpvSharePercentage()
                                                ==
                                                null
                                                ? "."
                                                : " at "
                                                        +
                                                        dashboard
                                                                .market()
                                                                .leadingCpvSharePercentage()
                                                        +
                                                        "% of observed CPV activity."
                                ),

                        null,

                        "/market"
                )
        );
    }


    private AlertSignal signal(
            String fingerprint,
            NotificationType type,
            NotificationSeverity severity,
            String title,
            String detail,
            UUID tenderId,
            String href
    ) {

        return new AlertSignal(
                fingerprint,
                type,
                severity,
                title,
                detail,
                tenderId,
                href
        );
    }


    private void add(
            Map<String, AlertSignal> signals,
            AlertSignal signal
    ) {

        signals.put(
                signal.fingerprint(),
                signal
        );
    }


    private NotificationSeverity severity(
            DashboardActionPriority priority
    ) {

        return switch (
                priority
        ) {

            case CRITICAL ->
                    NotificationSeverity.CRITICAL;

            case HIGH ->
                    NotificationSeverity.HIGH;

            case MEDIUM ->
                    NotificationSeverity.MEDIUM;

            case LOW ->
                    NotificationSeverity.LOW;
        };
    }


    private int severityRank(
            NotificationSeverity severity
    ) {

        return switch (
                severity
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


    private String label(
            WatchlistItemSnapshot item,
            String fallback
    ) {

        if (
                item.label() != null
                &&
                !item.label().isBlank()
        ) {

            return item.label();
        }


        return fallback == null
                ||
                fallback.isBlank()
                ? item.targetKey()
                : fallback;
    }


    private String plural(
            long count
    ) {

        return count == 1
                ? ""
                : "s";
    }

}
