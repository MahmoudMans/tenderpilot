package com.tenderpilot.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import com.tenderpilot.procurement.quality.domain.DataQualityGrade;

import com.tenderpilot.search.domain.ProcurementSearchQuery;

import com.tenderpilot.search.infrastructure.OpenSearchIndexDefinition;
import com.tenderpilot.search.infrastructure.OpenSearchQueryBuilder;
import com.tenderpilot.search.infrastructure.OpenSearchSettings;

import java.util.Map;

import org.junit.jupiter.api.Test;

class OpenSearchSearchContractTest {

    private final OpenSearchSettings settings =
            new OpenSearchSettings(
                    true,
                    true,
                    "http://localhost:9200",
                    "tenderpilot-opportunities-v1",
                    25,
                    100
            );


    private final OpenSearchQueryBuilder queryBuilder =
            new OpenSearchQueryBuilder(
                    settings
            );


    @Test
    void indexMappingIsStrictAndContainsCoreSearchFields() {

        Map<String, Object> body =
                new OpenSearchIndexDefinition()
                        .createIndexBody();


        String representation =
                body.toString();


        assertThat(
                representation
        )
                .contains(
                        "dynamic=strict"
                )
                .contains(
                        "title={type=text}"
                )
                .contains(
                        "cpvCodes={type=keyword}"
                )
                .contains(
                        "qualityScore={type=integer}"
                )
                .contains(
                        "supplierNames={type=text}"
                );

    }


    @Test
    void queryUsesWeightedFullTextSearch() {

        Map<String, Object> body =
                queryBuilder
                        .build(
                                new ProcurementSearchQuery(
                                        "energy renovation",
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        25
                                )
                        );


        String representation =
                body.toString();


        assertThat(
                representation
        )
                .contains(
                        "multi_match"
                )
                .contains(
                        "title^5"
                )
                .contains(
                        "buyerName^4"
                )
                .contains(
                        "cpvCodes^4"
                )
                .contains(
                        "fuzziness=AUTO"
                );

    }


    @Test
    void querySupportsStructuredProcurementFilters() {

        Map<String, Object> body =
                queryBuilder
                        .build(
                                new ProcurementSearchQuery(
                                        "renovation",
                                        ProcurementStatus.PUBLISHED,
                                        ProcurementSource.BOAMP,
                                        ContractNature.WORKS,
                                        "fr",
                                        "Île-de-France",
                                        75,
                                        DataQualityGrade.GOOD,
                                        50
                                )
                        );


        String representation =
                body.toString();


        assertThat(
                representation
        )
                .contains(
                        "PUBLISHED"
                )
                .contains(
                        "BOAMP"
                )
                .contains(
                        "WORKS"
                )
                .contains(
                        "countryCode=FR"
                )
                .contains(
                        "Île-de-France"
                )
                .contains(
                        "qualityScore"
                )
                .contains(
                        "GOOD"
                );

    }


    @Test
    void resultSizeIsBoundedAndBlankQueryUsesMatchAll() {

        Map<String, Object> body =
                queryBuilder
                        .build(
                                new ProcurementSearchQuery(
                                        " ",
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        1000
                                )
                        );


        assertThat(
                body.get(
                        "size"
                )
        )
                .isEqualTo(
                        100
                );


        assertThat(
                body.toString()
        )
                .contains(
                        "match_all"
                );

    }

}
