package com.tenderpilot.dataoperations.domain;

import java.time.Instant;

public record IngestionRunObservation(

        String source,

        String rawStatus,

        Instant startedAt,

        Instant finishedAt,

        Long importedCount,

        Long rejectedCount

) {
}
