package com.tenderpilot.dataoperations;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.dataoperations.infrastructure.IngestionRunSchemaInspector;
import com.tenderpilot.dataoperations.infrastructure.JdbcIngestionRunGateway;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_data_operations;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=false",
                "tenderpilot.search.auto-index=false",
                "tenderpilot.local-ai.enabled=false",
                "tenderpilot.rag.enabled=false"
        }
)
@ActiveProfiles(
        "test"
)
class DataOperationsSchemaIntegrationTest {

    @Autowired
    private IngestionRunSchemaInspector
            schemaInspector;


    @Autowired
    private JdbcIngestionRunGateway
            gateway;


    @Test
    void canonicalIngestionRunTableAndCoreColumnsAreDiscoverable() {

        var schema =
                schemaInspector
                        .schema();


        assertThat(
                schema.table()
        )
                .isEqualToIgnoringCase(
                        "data_source_ingestion_runs"
                );


        assertThat(
                schema.source()
        )
                .isNotBlank();


        assertThat(
                schema.startedAt()
        )
                .isNotBlank();
    }


    @Test
    void emptyCanonicalRunTableCanBeQueriedSafely() {

        assertThat(
                gateway.recentRuns(
                        50
                )
        )
                .isEmpty();
    }

}
