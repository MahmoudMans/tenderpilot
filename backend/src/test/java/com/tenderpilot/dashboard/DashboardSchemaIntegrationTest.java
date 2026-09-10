package com.tenderpilot.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.dashboard.infrastructure.DashboardAnalyticsSchemaInspector;
import com.tenderpilot.dashboard.infrastructure.JdbcDashboardMatchGateway;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_dashboard_schema;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=false",
                "tenderpilot.search.auto-index=false",
                "tenderpilot.local-ai.enabled=false",
                "tenderpilot.rag.enabled=false"
        }
)
@ActiveProfiles(
        "test"
)
class DashboardSchemaIntegrationTest {

    @Autowired
    private DashboardAnalyticsSchemaInspector
            schemaInspector;


    @Autowired
    private JdbcDashboardMatchGateway
            matchGateway;


    @Test
    void canonicalMatchSchemaIsDiscoverable() {

        var schema =
                schemaInspector.schema();


        assertThat(
                schema.matchTable()
        )
                .isEqualToIgnoringCase(
                        "organization_tender_matches"
                );


        assertThat(
                schema.organizationId()
        )
                .isEqualToIgnoringCase(
                        "organization_id"
                );


        assertThat(
                schema.tenderId()
        )
                .isEqualToIgnoringCase(
                        "tender_id"
                );


        assertThat(
                schema.score()
        )
                .isNotBlank();


        assertThat(
                schema.grade()
        )
                .isNotBlank();


        assertThat(
                schema.blocked()
        )
                .isNotBlank();
    }


    @Test
    void unknownOrganizationReturnsEmptyMatchPortfolioWithoutSchemaFailure() {

        var result =
                matchGateway.matches(
                        UUID.fromString(
                                "20999999-9999-9999-9999-999999999999"
                        )
                );


        assertThat(
                result
        )
                .isEmpty();
    }

}
