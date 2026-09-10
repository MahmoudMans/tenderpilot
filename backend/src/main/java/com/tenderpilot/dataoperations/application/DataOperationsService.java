package com.tenderpilot.dataoperations.application;

import com.tenderpilot.dataoperations.api.DataOperationsResponse;

import com.tenderpilot.dataoperations.domain.IngestionRunObservation;
import com.tenderpilot.dataoperations.domain.IngestionRunOutcome;
import com.tenderpilot.dataoperations.domain.SourceHealthAnalysis;
import com.tenderpilot.dataoperations.domain.SourceHealthStatus;

import java.time.Duration;
import java.time.Instant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class DataOperationsService {

    private static final List<String> CORE_SOURCES =
            List.of(
                    "TED",
                    "BOAMP",
                    "DECP"
            );


    private final IngestionRunGateway
            ingestionRunGateway;


    private final SourceHealthAnalyzer
            healthAnalyzer;


    public DataOperationsService(
            IngestionRunGateway ingestionRunGateway,
            SourceHealthAnalyzer healthAnalyzer
    ) {

        this.ingestionRunGateway =
                ingestionRunGateway;

        this.healthAnalyzer =
                healthAnalyzer;
    }


    public DataOperationsResponse overview(
            Integer historyLimit
    ) {

        int resolvedHistoryLimit =
                historyLimit == null
                        ? 50
                        : Math.max(
                                10,
                                Math.min(
                                        200,
                                        historyLimit
                                )
                        );


        Instant now =
                Instant.now();


        List<IngestionRunObservation> allRuns =
                ingestionRunGateway
                        .recentRuns(
                                5000
                        )
                        .stream()
                        .filter(
                                run ->
                                        run.source() != null
                                        &&
                                        run.startedAt() != null
                        )
                        .toList();


        Map<String, List<IngestionRunObservation>> bySource =
                runsBySource(
                        allRuns
                );


        Set<String> sourceNames =
                new LinkedHashSet<>(
                        CORE_SOURCES
                );


        sourceNames.addAll(
                bySource.keySet()
        );


        List<SourceHealthAnalysis> analyses =
                sourceNames
                        .stream()
                        .map(
                                source ->
                                        healthAnalyzer
                                                .analyze(
                                                        source,
                                                        bySource.getOrDefault(
                                                                source,
                                                                List.of()
                                                        ),
                                                        now
                                                )
                        )
                        .sorted(
                                Comparator
                                        .<SourceHealthAnalysis>comparingInt(
                                                analysis ->
                                                        healthRank(
                                                                analysis.health()
                                                        )
                                        )
                                        .thenComparing(
                                                SourceHealthAnalysis::source
                                        )
                        )
                        .toList();


        return new DataOperationsResponse(
                now,

                summary(
                        analyses,
                        allRuns,
                        now
                ),

                analyses
                        .stream()
                        .map(
                                this::sourceResponse
                        )
                        .toList(),

                allRuns
                        .stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                IngestionRunObservation::startedAt
                                        )
                                        .reversed()
                        )
                        .limit(
                                resolvedHistoryLimit
                        )
                        .map(
                                this::runResponse
                        )
                        .toList()
        );
    }


    private DataOperationsResponse.Summary summary(
            List<SourceHealthAnalysis> analyses,
            List<IngestionRunObservation> runs,
            Instant now
    ) {

        Instant oneDayAgo =
                now.minus(
                        Duration.ofDays(
                                1
                        )
                );


        Instant sevenDaysAgo =
                now.minus(
                        Duration.ofDays(
                                7
                        )
                );


        List<IngestionRunObservation> runs24h =
                runs
                        .stream()
                        .filter(
                                run ->
                                        !run
                                                .startedAt()
                                                .isBefore(
                                                        oneDayAgo
                                                )
                        )
                        .toList();


        List<IngestionRunObservation> runs7d =
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


        int failures24h =
                (int)
                        runs24h
                                .stream()
                                .filter(
                                        run ->
                                                healthAnalyzer.outcome(
                                                        run.rawStatus(),
                                                        run.finishedAt()
                                                )
                                                        ==
                                                        IngestionRunOutcome.FAILURE
                                )
                                .count();


        int successes7d =
                (int)
                        runs7d
                                .stream()
                                .filter(
                                        run ->
                                                healthAnalyzer.outcome(
                                                        run.rawStatus(),
                                                        run.finishedAt()
                                                )
                                                        ==
                                                        IngestionRunOutcome.SUCCESS
                                )
                                .count();


        int failures7d =
                (int)
                        runs7d
                                .stream()
                                .filter(
                                        run ->
                                                healthAnalyzer.outcome(
                                                        run.rawStatus(),
                                                        run.finishedAt()
                                                )
                                                        ==
                                                        IngestionRunOutcome.FAILURE
                                )
                                .count();


        int terminal7d =
                successes7d
                        +
                        failures7d;


        Integer successRate =
                terminal7d == 0
                        ? null
                        : (int)
                                Math.round(
                                        successes7d
                                                *
                                                100.0
                                                /
                                                terminal7d
                                );


        return new DataOperationsResponse
                .Summary(
                        analyses.size(),

                        healthCount(
                                analyses,
                                SourceHealthStatus.HEALTHY
                        ),

                        healthCount(
                                analyses,
                                SourceHealthStatus.DEGRADED
                        ),

                        healthCount(
                                analyses,
                                SourceHealthStatus.UNHEALTHY
                        ),

                        healthCount(
                                analyses,
                                SourceHealthStatus.STALE
                        ),

                        healthCount(
                                analyses,
                                SourceHealthStatus.RUNNING
                        ),

                        healthCount(
                                analyses,
                                SourceHealthStatus.UNKNOWN
                        ),

                        runs24h.size(),

                        failures24h,

                        runs7d.size(),

                        successRate
                );
    }


    private int healthCount(
            List<SourceHealthAnalysis> analyses,
            SourceHealthStatus health
    ) {

        return (int)
                analyses
                        .stream()
                        .filter(
                                analysis ->
                                        analysis.health()
                                                ==
                                                health
                        )
                        .count();
    }


    private DataOperationsResponse.Source sourceResponse(
            SourceHealthAnalysis analysis
    ) {

        return new DataOperationsResponse
                .Source(
                        analysis.source(),
                        analysis.health(),
                        analysis.runCount7Days(),
                        analysis.successCount7Days(),
                        analysis.failureCount7Days(),
                        analysis.successRate7Days(),
                        analysis.failureStreak(),
                        analysis.lastStartedAt(),
                        analysis.lastFinishedAt(),
                        analysis.lastSuccessAt(),
                        analysis.lastFailureAt(),
                        analysis.importedCount7Days(),
                        analysis.rejectedCount7Days()
                );
    }


    private DataOperationsResponse.RecentRun runResponse(
            IngestionRunObservation run
    ) {

        Long duration =
                durationSeconds(
                        run.startedAt(),
                        run.finishedAt()
                );


        return new DataOperationsResponse
                .RecentRun(
                        normalizedSource(
                                run.source()
                        ),

                        healthAnalyzer
                                .outcome(
                                        run.rawStatus(),
                                        run.finishedAt()
                                ),

                        run.rawStatus(),

                        run.startedAt(),

                        run.finishedAt(),

                        duration,

                        run.importedCount(),

                        run.rejectedCount()
                );
    }


    private Long durationSeconds(
            Instant started,
            Instant finished
    ) {

        if (
                started == null
                ||
                finished == null
        ) {

            return null;
        }


        long seconds =
                Duration
                        .between(
                                started,
                                finished
                        )
                        .getSeconds();


        return seconds < 0
                ? null
                : seconds;
    }


    private Map<String, List<IngestionRunObservation>> runsBySource(
            List<IngestionRunObservation> runs
    ) {

        Map<String, List<IngestionRunObservation>> result =
                new LinkedHashMap<>();


        for (
                IngestionRunObservation run :
                runs
        ) {

            String source =
                    normalizedSource(
                            run.source()
                    );


            if (
                    source == null
            ) {

                continue;
            }


            result
                    .computeIfAbsent(
                            source,
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            run
                    );
        }


        return result;
    }


    private String normalizedSource(
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


    private int healthRank(
            SourceHealthStatus health
    ) {

        return switch (
                health
        ) {

            case UNHEALTHY ->
                    0;

            case DEGRADED ->
                    1;

            case STALE ->
                    2;

            case RUNNING ->
                    3;

            case UNKNOWN ->
                    4;

            case HEALTHY ->
                    5;
        };
    }

}
