package com.tenderpilot.datasource.domain;

import com.tenderpilot.procurement.domain.ProcurementSource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "data_source_ingestion_runs"
)
public class IngestionRunEntity {

    @Id
    private UUID id;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "source",
            nullable = false,
            length = 40
    )
    private ProcurementSource source;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private IngestionRunStatus status;


    @Column(
            name = "query_text",
            nullable = false,
            length = 2000
    )
    private String queryText;


    @Column(
            name = "requested_limit",
            nullable = false
    )
    private int requestedLimit;


    @Column(
            name = "discovered_count",
            nullable = false
    )
    private int discoveredCount;


    @Column(
            name = "processed_count",
            nullable = false
    )
    private int processedCount;


    @Column(
            name = "failed_count",
            nullable = false
    )
    private int failedCount;


    @Column(
            name = "started_at",
            nullable = false
    )
    private Instant startedAt;


    @Column(
            name = "completed_at"
    )
    private Instant completedAt;


    @Column(
            name = "last_error",
            length = 1500
    )
    private String lastError;


    protected IngestionRunEntity() {
    }


    private IngestionRunEntity(

            ProcurementSource source,

            String queryText,

            int requestedLimit

    ) {

        this.id =
                UUID.randomUUID();

        this.source =
                source;

        this.status =
                IngestionRunStatus.RUNNING;

        this.queryText =
                queryText;

        this.requestedLimit =
                requestedLimit;

        this.startedAt =
                Instant.now();

    }


    public static IngestionRunEntity start(

            ProcurementSource source,

            String queryText,

            int requestedLimit

    ) {

        return new IngestionRunEntity(

                source,

                queryText,

                requestedLimit

        );

    }


    public void complete(

            int discoveredCount,

            int processedCount,

            int failedCount,

            String lastError

    ) {

        this.discoveredCount =
                discoveredCount;

        this.processedCount =
                processedCount;

        this.failedCount =
                failedCount;

        this.lastError =
                truncate(
                        lastError
                );


        if (
                failedCount == 0
        ) {

            this.status =
                    IngestionRunStatus.COMPLETED;

        } else if (
                processedCount > 0
        ) {

            this.status =
                    IngestionRunStatus.PARTIAL;

        } else {

            this.status =
                    IngestionRunStatus.FAILED;

        }


        this.completedAt =
                Instant.now();

    }


    public void fail(
            String error
    ) {

        this.status =
                IngestionRunStatus.FAILED;

        this.lastError =
                truncate(
                        error
                );

        this.completedAt =
                Instant.now();

    }


    @PrePersist
    void beforeInsert() {

        if (
                id == null
        ) {

            id =
                    UUID.randomUUID();

        }


        if (
                status == null
        ) {

            status =
                    IngestionRunStatus.RUNNING;

        }


        if (
                startedAt == null
        ) {

            startedAt =
                    Instant.now();

        }

    }


    private String truncate(
            String value
    ) {

        if (
                value == null
        ) {

            return null;

        }


        return value.length()
                <=
                1500

                ? value

                : value.substring(
                        0,
                        1500
                );

    }


    public UUID getId() {

        return id;

    }


    public ProcurementSource getSource() {

        return source;

    }


    public IngestionRunStatus getStatus() {

        return status;

    }


    public String getQueryText() {

        return queryText;

    }


    public int getRequestedLimit() {

        return requestedLimit;

    }


    public int getDiscoveredCount() {

        return discoveredCount;

    }


    public int getProcessedCount() {

        return processedCount;

    }


    public int getFailedCount() {

        return failedCount;

    }


    public Instant getStartedAt() {

        return startedAt;

    }


    public Instant getCompletedAt() {

        return completedAt;

    }


    public String getLastError() {

        return lastError;

    }

}
