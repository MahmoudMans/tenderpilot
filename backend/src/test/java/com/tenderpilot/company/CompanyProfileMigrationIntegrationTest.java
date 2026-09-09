package com.tenderpilot.company;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(
        "test"
)
class CompanyProfileMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void liquibaseCreatesCompanyIntelligenceTables() {

        List<String> tables =

                List.of(

                        "company_profiles",

                        "company_capabilities",

                        "company_operating_regions",

                        "company_target_cpvs",

                        "company_certifications",

                        "company_reference_projects",

                        "company_preferred_buyer_types",

                        "company_excluded_keywords"

                );


        for (
                String table :
                tables
        ) {

            Integer count =

                    jdbcTemplate
                            .queryForObject(

                                    """
                                    select count(*)
                                    from information_schema.tables
                                    where lower(table_name) = ?
                                    """,

                                    Integer.class,

                                    table

                            );


            assertThat(
                    count
            )
                    .as(
                            "Expected migrated table %s",
                            table
                    )
                    .isEqualTo(
                            1
                    );

        }

    }

}
