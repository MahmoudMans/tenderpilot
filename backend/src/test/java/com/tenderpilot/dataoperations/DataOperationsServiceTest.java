package com.tenderpilot.dataoperations;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.dataoperations.application.DataOperationsService;
import com.tenderpilot.dataoperations.application.IngestionRunGateway;
import com.tenderpilot.dataoperations.application.SourceHealthAnalyzer;

import com.tenderpilot.dataoperations.domain.IngestionRunObservation;
import com.tenderpilot.dataoperations.domain.SourceHealthStatus;

import java.time.Duration;
import java.time.Instant;

import java.util.List;

import org.junit.jupiter.api.Test;

class DataOperationsServiceTest {

    @Test
    void coreSourcesRemainVisibleEvenWithoutRunHistory() {

        DataOperationsService service =
                new DataOperationsService(
                        limit ->
                                List.of(),

                        new SourceHealthAnalyzer()
                );


        var result =
                service.overview(
                        50
                );


        assertThat(
                result.sources()
                        .stream()
                        .map(
                                source ->
                                        source.source()
                        )
        )
                .containsExactly(
                        "BOAMP",
                        "DECP",
                        "TED"
                );


        assertThat(
                result.summary()
                        .unknownSourceCount()
        )
                .isEqualTo(
                        3
                );
    }


    @Test
    void overviewAggregatesSourceHealthAndReliability() {

        Instant now =
                Instant.now();


        IngestionRunGateway gateway =
                limit ->
                        List.of(
                                new IngestionRunObservation(
                                        "TED",
                                        "SUCCESS",
                                        now.minus(
                                                Duration.ofHours(
                                                        2
                                                )
                                        ),
                                        now.minus(
                                                Duration.ofHours(
                                                        1
                                                )
                                        ),
                                        500L,
                                        5L
                                ),

                                new IngestionRunObservation(
                                        "BOAMP",
                                        "FAILED",
                                        now.minus(
                                                Duration.ofHours(
                                                        3
                                                )
                                        ),
                                        now.minus(
                                                Duration.ofHours(
                                                        2
                                                )
                                        ),
                                        0L,
                                        100L
                                ),

                                new IngestionRunObservation(
                                        "DECP",
                                        "RUNNING",
                                        now.minus(
                                                Duration.ofMinutes(
                                                        30
                                                )
                                        ),
                                        null,
                                        null,
                                        null
                                )
                        );


        DataOperationsService service =
                new DataOperationsService(
                        gateway,
                        new SourceHealthAnalyzer()
                );


        var result =
                service.overview(
                        50
                );


        assertThat(
                result.summary()
                        .runs24Hours()
        )
                .isEqualTo(
                        3
                );


        assertThat(
                result.summary()
                        .failures24Hours()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.summary()
                        .successRate7Days()
        )
                .isEqualTo(
                        50
                );


        assertThat(
                result.sources()
                        .stream()
                        .filter(
                                source ->
                                        source.source()
                                                .equals(
                                                        "TED"
                                                )
                        )
                        .findFirst()
                        .orElseThrow()
                        .health()
        )
                .isEqualTo(
                        SourceHealthStatus.HEALTHY
                );
    }


    @Test
    void historyLimitIsAppliedToRecentRuns() {

        Instant now =
                Instant.now();


        IngestionRunGateway gateway =
                limit ->
                        java.util.stream.IntStream
                                .range(
                                        0,
                                        40
                                )
                                .mapToObj(
                                        index ->
                                                new IngestionRunObservation(
                                                        "TED",
                                                        "SUCCESS",
                                                        now.minusSeconds(
                                                                index
                                                                        *
                                                                        60L
                                                        ),
                                                        now.minusSeconds(
                                                                index
                                                                        *
                                                                        60L
                                                                        -
                                                                        30L
                                                        ),
                                                        10L,
                                                        0L
                                                )
                                )
                                .toList();


        var result =
                new DataOperationsService(
                        gateway,
                        new SourceHealthAnalyzer()
                )
                        .overview(
                                12
                        );


        assertThat(
                result.recentRuns()
        )
                .hasSize(
                        12
                );
    }


    @Test
    void recentRunDurationIsCalculatedWithoutMutatingIngestion() {

        Instant started =
                Instant.now()
                        .minusSeconds(
                                300
                        );


        Instant finished =
                started.plusSeconds(
                        125
                );


        var result =
                new DataOperationsService(
                        limit ->
                                List.of(
                                        new IngestionRunObservation(
                                                "TED",
                                                "SUCCESS",
                                                started,
                                                finished,
                                                20L,
                                                1L
                                        )
                                ),

                        new SourceHealthAnalyzer()
                )
                        .overview(
                                20
                        );


        assertThat(
                result
                        .recentRuns()
                        .getFirst()
                        .durationSeconds()
        )
                .isEqualTo(
                        125L
                );
    }

}
