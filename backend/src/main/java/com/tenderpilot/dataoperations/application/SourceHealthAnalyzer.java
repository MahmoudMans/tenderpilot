package com.tenderpilot.dataoperations.application;

import com.tenderpilot.dataoperations.domain.IngestionRunObservation;
import com.tenderpilot.dataoperations.domain.IngestionRunOutcome;
import com.tenderpilot.dataoperations.domain.SourceHealthAnalysis;
import com.tenderpilot.dataoperations.domain.SourceHealthStatus;

import java.time.Duration;
import java.time.Instant;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class SourceHealthAnalyzer {

    private static final Duration HEALTHY_WINDOW =
            Duration.ofHours(
                    48
            );


    private static final Duration DEGRADED_SUCCESS_WINDOW =
            Duration.ofDays(
                    1
            );


    private static final Duration ANALYTICS_WINDOW =
            Duration.ofDays(
                    7
            );


    public SourceHealthAnalysis analyze(
            String source,
            List<IngestionRunObservation> observations,
            Instant now
    ) {

        List<IngestionRunObservation> runs =
                observations == null
                        ? List.of()
                        : observations
                                .stream()
                                .filter(
                                        observation ->
                                                observation != null
                                                &&
                                                observation.startedAt() != null
                                )
                                .sorted(
                                        Comparator
                                                .comparing(
                                                        IngestionRunObservation::startedAt
                                                )
                                                .reversed()
                                )
                                .toList();


        if (
                runs.isEmpty()
        ) {

            return new SourceHealthAnalysis(
                    source,
                    SourceHealthStatus.UNKNOWN,
                    0,
                    0,
                    0,
                    null,
                    0,
                    null,
                    null,
                    null,
                    null,
                    0,
                    0
            );
        }


        Instant sevenDaysAgo =
                now.minus(
                        ANALYTICS_WINDOW
                );


        List<IngestionRunObservation> recent =
                runs
                        .stream()
                        .filter(
                                run ->
                                        !run
                                                .startedAt()
                                                .isBefore(
                                                        sevenDaysAgo
                                                )
                        )
                        .toList();


        int successes =
                (int)
                        recent
                                .stream()
                                .filter(
                                        run ->
                                                outcome(
                                                        run.rawStatus(),
                                                        run.finishedAt()
                                                )
                                                        ==
                                                        IngestionRunOutcome.SUCCESS
                                )
                                .count();


        int failures =
                (int)
                        recent
                                .stream()
                                .filter(
                                        run ->
                                                outcome(
                                                        run.rawStatus(),
                                                        run.finishedAt()
                                                )
                                                        ==
                                                        IngestionRunOutcome.FAILURE
                                )
                                .count();


        int terminalRuns =
                successes
                        +
                        failures;


        Integer successRate =
                terminalRuns == 0
                        ? null
                        : (int)
                                Math.round(
                                        successes
                                                *
                                                100.0
                                                /
                                                terminalRuns
                                );


        Instant lastSuccess =
                runs
                        .stream()
                        .filter(
                                run ->
                                        outcome(
                                                run.rawStatus(),
                                                run.finishedAt()
                                        )
                                                ==
                                                IngestionRunOutcome.SUCCESS
                        )
                        .map(
                                run ->
                                        run.finishedAt() == null
                                                ? run.startedAt()
                                                : run.finishedAt()
                        )
                        .findFirst()
                        .orElse(
                                null
                        );


        Instant lastFailure =
                runs
                        .stream()
                        .filter(
                                run ->
                                        outcome(
                                                run.rawStatus(),
                                                run.finishedAt()
                                        )
                                                ==
                                                IngestionRunOutcome.FAILURE
                        )
                        .map(
                                run ->
                                        run.finishedAt() == null
                                                ? run.startedAt()
                                                : run.finishedAt()
                        )
                        .findFirst()
                        .orElse(
                                null
                        );


        IngestionRunObservation latest =
                runs.getFirst();


        IngestionRunOutcome latestOutcome =
                outcome(
                        latest.rawStatus(),
                        latest.finishedAt()
                );


        SourceHealthStatus health =
                health(
                        latestOutcome,
                        latest,
                        lastSuccess,
                        now
                );


        return new SourceHealthAnalysis(
                source,

                health,

                recent.size(),

                successes,

                failures,

                successRate,

                failureStreak(
                        runs
                ),

                latest.startedAt(),

                latest.finishedAt(),

                lastSuccess,

                lastFailure,

                recent
                        .stream()
                        .map(
                                IngestionRunObservation::importedCount
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .mapToLong(
                                Long::longValue
                        )
                        .sum(),

                recent
                        .stream()
                        .map(
                                IngestionRunObservation::rejectedCount
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .mapToLong(
                                Long::longValue
                        )
                        .sum()
        );
    }


    public IngestionRunOutcome outcome(
            String rawStatus,
            Instant finishedAt
    ) {

        if (
                rawStatus == null
                ||
                rawStatus.isBlank()
        ) {

            return finishedAt == null
                    ? IngestionRunOutcome.RUNNING
                    : IngestionRunOutcome.UNKNOWN;
        }


        String value =
                rawStatus
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        return switch (
                value
        ) {

            case
                    "SUCCESS",
                    "SUCCEEDED",
                    "COMPLETED",
                    "COMPLETE",
                    "DONE" ->

                    IngestionRunOutcome.SUCCESS;


            case
                    "FAILED",
                    "FAILURE",
                    "ERROR",
                    "ERRORED" ->

                    IngestionRunOutcome.FAILURE;


            case
                    "RUNNING",
                    "STARTED",
                    "IN_PROGRESS",
                    "PROCESSING" ->

                    IngestionRunOutcome.RUNNING;


            default ->
                    IngestionRunOutcome.UNKNOWN;
        };
    }


    private SourceHealthStatus health(
            IngestionRunOutcome latestOutcome,
            IngestionRunObservation latest,
            Instant lastSuccess,
            Instant now
    ) {

        if (
                latestOutcome
                        ==
                        IngestionRunOutcome.RUNNING
        ) {

            return SourceHealthStatus.RUNNING;
        }


        if (
                latestOutcome
                        ==
                        IngestionRunOutcome.FAILURE
        ) {

            if (
                    lastSuccess != null
                    &&
                    !lastSuccess.isBefore(
                            now.minus(
                                    DEGRADED_SUCCESS_WINDOW
                            )
                    )
            ) {

                return SourceHealthStatus.DEGRADED;
            }


            return SourceHealthStatus.UNHEALTHY;
        }


        if (
                latestOutcome
                        ==
                        IngestionRunOutcome.SUCCESS
        ) {

            Instant reference =
                    latest.finishedAt() == null
                            ? latest.startedAt()
                            : latest.finishedAt();


            if (
                    !reference.isBefore(
                            now.minus(
                                    HEALTHY_WINDOW
                            )
                    )
            ) {

                return SourceHealthStatus.HEALTHY;
            }


            return SourceHealthStatus.STALE;
        }


        return SourceHealthStatus.UNKNOWN;
    }


    private int failureStreak(
            List<IngestionRunObservation> runs
    ) {

        int result =
                0;


        for (
                IngestionRunObservation run :
                runs
        ) {

            IngestionRunOutcome outcome =
                    outcome(
                            run.rawStatus(),
                            run.finishedAt()
                    );


            if (
                    outcome
                            ==
                            IngestionRunOutcome.RUNNING
            ) {

                continue;
            }


            if (
                    outcome
                            ==
                            IngestionRunOutcome.FAILURE
            ) {

                result++;

                continue;
            }


            if (
                    outcome
                            ==
                            IngestionRunOutcome.SUCCESS
            ) {

                break;
            }
        }


        return result;
    }

}
