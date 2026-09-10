package com.tenderpilot.dataoperations;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.dataoperations.application.SourceHealthAnalyzer;

import com.tenderpilot.dataoperations.domain.IngestionRunObservation;
import com.tenderpilot.dataoperations.domain.SourceHealthStatus;

import java.time.Duration;
import java.time.Instant;

import java.util.List;

import org.junit.jupiter.api.Test;

class SourceHealthAnalyzerTest {

    private static final Instant NOW =
            Instant.parse(
                    "2026-09-10T12:00:00Z"
            );


    private final SourceHealthAnalyzer analyzer =
            new SourceHealthAnalyzer();


    @Test
    void recentSuccessfulRunIsHealthy() {

        var analysis =
                analyzer.analyze(
                        "TED",

                        List.of(
                                run(
                                        "SUCCESS",
                                        NOW.minus(
                                                Duration.ofHours(
                                                        4
                                                )
                                        ),
                                        NOW.minus(
                                                Duration.ofHours(
                                                        3
                                                )
                                        )
                                )
                        ),

                        NOW
                );


        assertThat(
                analysis.health()
        )
                .isEqualTo(
                        SourceHealthStatus.HEALTHY
                );


        assertThat(
                analysis.successRate7Days()
        )
                .isEqualTo(
                        100
                );
    }


    @Test
    void oldSuccessfulRunIsStale() {

        var analysis =
                analyzer.analyze(
                        "BOAMP",

                        List.of(
                                run(
                                        "COMPLETED",
                                        NOW.minus(
                                                Duration.ofDays(
                                                        4
                                                )
                                        ),
                                        NOW.minus(
                                                Duration.ofDays(
                                                        4
                                                )
                                        )
                                )
                        ),

                        NOW
                );


        assertThat(
                analysis.health()
        )
                .isEqualTo(
                        SourceHealthStatus.STALE
                );
    }


    @Test
    void recentFailureAfterRecentSuccessIsDegraded() {

        var analysis =
                analyzer.analyze(
                        "DECP",

                        List.of(
                                run(
                                        "FAILED",
                                        NOW.minus(
                                                Duration.ofHours(
                                                        2
                                                )
                                        ),
                                        NOW.minus(
                                                Duration.ofHours(
                                                        1
                                                )
                                        )
                                ),

                                run(
                                        "SUCCESS",
                                        NOW.minus(
                                                Duration.ofHours(
                                                        10
                                                )
                                        ),
                                        NOW.minus(
                                                Duration.ofHours(
                                                        9
                                                )
                                        )
                                )
                        ),

                        NOW
                );


        assertThat(
                analysis.health()
        )
                .isEqualTo(
                        SourceHealthStatus.DEGRADED
                );


        assertThat(
                analysis.failureStreak()
        )
                .isEqualTo(
                        1
                );
    }


    @Test
    void repeatedFailureWithoutRecentSuccessIsUnhealthy() {

        var analysis =
                analyzer.analyze(
                        "TED",

                        List.of(
                                run(
                                        "ERROR",
                                        NOW.minus(
                                                Duration.ofHours(
                                                        2
                                                )
                                        ),
                                        NOW.minus(
                                                Duration.ofHours(
                                                        1
                                                )
                                        )
                                ),

                                run(
                                        "FAILED",
                                        NOW.minus(
                                                Duration.ofHours(
                                                        7
                                                )
                                        ),
                                        NOW.minus(
                                                Duration.ofHours(
                                                        6
                                                )
                                        )
                                )
                        ),

                        NOW
                );


        assertThat(
                analysis.health()
        )
                .isEqualTo(
                        SourceHealthStatus.UNHEALTHY
                );


        assertThat(
                analysis.failureStreak()
        )
                .isEqualTo(
                        2
                );
    }


    @Test
    void unfinishedRunningRunHasRunningHealth() {

        var analysis =
                analyzer.analyze(
                        "TED",

                        List.of(
                                new IngestionRunObservation(
                                        "TED",
                                        "RUNNING",
                                        NOW.minusSeconds(
                                                60
                                        ),
                                        null,
                                        null,
                                        null
                                )
                        ),

                        NOW
                );


        assertThat(
                analysis.health()
        )
                .isEqualTo(
                        SourceHealthStatus.RUNNING
                );
    }


    private static IngestionRunObservation run(
            String status,
            Instant started,
            Instant finished
    ) {

        return new IngestionRunObservation(
                "SOURCE",
                status,
                started,
                finished,
                100L,
                2L
        );
    }

}
