package com.tenderpilot.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.procurement.application.CanonicalTenderImportService;
import com.tenderpilot.procurement.application.CanonicalTenderUpsertRequest;

import com.tenderpilot.procurement.domain.BuyerCategory;
import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementProcedure;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import com.tenderpilot.search.api.ProcurementSearchResponse;

import com.tenderpilot.search.application.OpenSearchGateway;
import com.tenderpilot.search.application.ProcurementSearchService;
import com.tenderpilot.search.application.SearchProjectionService;
import com.tenderpilot.search.application.SearchReindexResult;

import com.tenderpilot.search.domain.ProcurementSearchDocument;
import com.tenderpilot.search.domain.ProcurementSearchQuery;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_search;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=true",
                "tenderpilot.search.auto-index=true"
        }
)
@ActiveProfiles(
        "test"
)
@Import(
        ProcurementSearchIntegrationTest
                .FakeSearchConfiguration.class
)
@Transactional
class ProcurementSearchIntegrationTest {

    @Autowired
    private CanonicalTenderImportService
            tenderImportService;


    @Autowired
    private ProcurementSearchService
            searchService;


    @Autowired
    private SearchProjectionService
            projectionService;


    @Autowired
    private FakeOpenSearchGateway
            fakeGateway;


    @BeforeEach
    void resetGateway() {

        fakeGateway.reset();

    }


    @Test
    void canonicalTenderImportAutomaticallyIndexesProjection() {

        UUID tenderId =
                tenderImportService
                        .upsert(
                                request(
                                        "SEARCH-AUTO-INDEX",
                                        "Energy renovation of public school"
                                )
                        );


        assertThat(
                fakeGateway.documents
        )
                .containsKey(
                        tenderId
                );


        ProcurementSearchDocument document =
                fakeGateway
                        .documents
                        .get(
                                tenderId
                        );


        assertThat(
                document.title()
        )
                .contains(
                        "Energy renovation"
                );


        assertThat(
                document.qualityScore()
        )
                .isEqualTo(
                        100
                );

    }


    @Test
    void searchReturnsOpenSearchRelevanceHitHydratedFromPostgresql() {

        UUID target =
                tenderImportService
                        .upsert(
                                request(
                                        "SEARCH-RELEVANCE-1",
                                        "School energy renovation programme"
                                )
                        );


        tenderImportService
                .upsert(
                        request(
                                "SEARCH-RELEVANCE-2",
                                "Municipal bridge maintenance"
                        )
                );


        ProcurementSearchResponse response =
                searchService
                        .search(
                                new ProcurementSearchQuery(
                                        "school energy",
                                        ProcurementStatus.PUBLISHED,
                                        ProcurementSource.MANUAL_DEMO,
                                        ContractNature.WORKS,
                                        "FR",
                                        "Île-de-France",
                                        90,
                                        null,
                                        25
                                )
                        );


        assertThat(
                response.fallback()
        )
                .isFalse();


        assertThat(
                response.engine()
        )
                .isEqualTo(
                        "OPENSEARCH"
                );


        assertThat(
                response.hits()
        )
                .isNotEmpty();


        assertThat(
                response
                        .hits()
                        .getFirst()
                        .opportunity()
                        .id()
        )
                .isEqualTo(
                        target
                );


        assertThat(
                response
                        .hits()
                        .getFirst()
                        .score()
        )
                .isPositive();

    }


    @Test
    void searchFallsBackToPostgresqlWhenOpenSearchFails() {

        UUID expected =
                tenderImportService
                        .upsert(
                                request(
                                        "SEARCH-FALLBACK",
                                        "Fallback bridge renovation"
                                )
                        );


        fakeGateway.failSearch =
                true;


        ProcurementSearchResponse response =
                searchService
                        .search(
                                new ProcurementSearchQuery(
                                        "Fallback bridge",
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


        assertThat(
                response.fallback()
        )
                .isTrue();


        assertThat(
                response.engine()
        )
                .isEqualTo(
                        "POSTGRESQL_FALLBACK"
                );


        assertThat(
                response.hits()
        )
                .extracting(
                        hit ->
                                hit
                                        .opportunity()
                                        .id()
                )
                .contains(
                        expected
                );

    }


    @Test
    void rebuildRecreatesEntireSearchProjection() {

        tenderImportService
                .upsert(
                        request(
                                "SEARCH-REBUILD-1",
                                "First procurement search fixture"
                        )
                );


        tenderImportService
                .upsert(
                        request(
                                "SEARCH-REBUILD-2",
                                "Second procurement search fixture"
                        )
                );


        fakeGateway.documents.clear();


        SearchReindexResult result =
                projectionService
                        .rebuild();


        assertThat(
                result.discoveredCount()
        )
                .isEqualTo(
                        2
                );


        assertThat(
                result.indexedCount()
        )
                .isEqualTo(
                        2
                );


        assertThat(
                result.failedCount()
        )
                .isZero();


        assertThat(
                fakeGateway.recreateCount
        )
                .isEqualTo(
                        1
                );


        assertThat(
                fakeGateway.documents
        )
                .hasSize(
                        2
                );

    }


    private CanonicalTenderUpsertRequest request(

            String externalId,

            String title

    ) {

        return new CanonicalTenderUpsertRequest(

                ProcurementSource.MANUAL_DEMO,

                externalId,

                "1",

                "SEARCH_TEST",

                "https://example.test/"
                        +
                        externalId,

                "search-checksum-"
                        +
                        externalId,

                Instant.parse(
                        "2026-09-09T08:00:00Z"
                ),

                new CanonicalTenderUpsertRequest
                        .BuyerInput(

                                "SEARCH-BUYER:"
                                        +
                                        externalId,

                                "Ville de Paris",

                                BuyerCategory.LOCAL_AUTHORITY,

                                "FR",

                                "Paris",

                                null,

                                null

                        ),

                title,

                title
                        +
                        " procurement description",

                ProcurementStatus.PUBLISHED,

                ProcurementProcedure.OPEN,

                ContractNature.WORKS,

                LocalDate.of(
                        2026,
                        9,
                        9
                ),

                Instant.parse(
                        "2026-10-20T16:00:00Z"
                ),

                new BigDecimal(
                        "1000000"
                ),

                new BigDecimal(
                        "2000000"
                ),

                "EUR",

                "FR",

                "IDF",

                "Île-de-France",

                false,

                false,

                true,

                List.of(

                        new CanonicalTenderUpsertRequest
                                .CpvInput(

                                        "45453000",

                                        "Overhaul and refurbishment work"

                                )

                ),

                List.of(

                        new CanonicalTenderUpsertRequest
                                .LocationInput(

                                        "FR",

                                        "IDF",

                                        "Île-de-France",

                                        "Paris"

                                )

                ),

                List.of()

        );

    }


    @TestConfiguration
    static class FakeSearchConfiguration {

        @Bean
        @Primary
        FakeOpenSearchGateway fakeOpenSearchGateway() {

            return new FakeOpenSearchGateway();

        }

    }


    static class FakeOpenSearchGateway
            implements OpenSearchGateway {

        private final Map<UUID, ProcurementSearchDocument>
                documents =
                new LinkedHashMap<>();


        private boolean failSearch;


        private int recreateCount;


        private boolean indexExists;


        void reset() {

            documents.clear();

            failSearch =
                    false;

            recreateCount =
                    0;

            indexExists =
                    false;

        }


        @Override
        public boolean isAvailable() {

            return true;

        }


        @Override
        public boolean indexExists() {

            return indexExists;

        }


        @Override
        public void ensureIndex() {

            indexExists =
                    true;

        }


        @Override
        public void recreateIndex() {

            documents.clear();

            indexExists =
                    true;

            recreateCount++;

        }


        @Override
        public void index(
                ProcurementSearchDocument document
        ) {

            indexExists =
                    true;

            documents.put(
                    document.id(),
                    document
            );

        }


        @Override
        public long count() {

            return documents.size();

        }


        @Override
        public SearchPage search(
                ProcurementSearchQuery query
        ) {

            if (
                    failSearch
            ) {

                throw new IllegalStateException(
                        "Synthetic OpenSearch failure"
                );

            }


            String text =
                    query.text()
                            ==
                            null

                            ? null

                            : query
                                    .text()
                                    .trim()
                                    .toLowerCase(
                                            Locale.ROOT
                                    );


            List<SearchMatch> matches =
                    new ArrayList<>();


            for (
                    ProcurementSearchDocument document :
                    documents.values()
            ) {

                if (
                        query.status()
                                !=
                                null
                        &&
                        !document
                                .status()
                                .equals(
                                        query
                                                .status()
                                                .name()
                                )
                ) {
                    continue;
                }


                if (
                        query.source()
                                !=
                                null
                        &&
                        !document
                                .sources()
                                .contains(
                                        query
                                                .source()
                                                .name()
                                )
                ) {
                    continue;
                }


                if (
                        query.contractNature()
                                !=
                                null
                        &&
                        !document
                                .contractNature()
                                .equals(
                                        query
                                                .contractNature()
                                                .name()
                                )
                ) {
                    continue;
                }


                if (
                        query.countryCode()
                                !=
                                null
                        &&
                        !document
                                .countryCode()
                                .equalsIgnoreCase(
                                        query.countryCode()
                                )
                ) {
                    continue;
                }


                if (
                        query.region()
                                !=
                                null
                        &&
                        (
                                document.regionName()
                                        ==
                                        null
                                ||
                                !document
                                        .regionName()
                                        .equalsIgnoreCase(
                                                query.region()
                                        )
                        )
                ) {
                    continue;
                }


                if (
                        query.minimumQualityScore()
                                !=
                                null
                        &&
                        (
                                document.qualityScore()
                                        ==
                                        null
                                ||
                                document.qualityScore()
                                        <
                                        query.minimumQualityScore()
                        )
                ) {
                    continue;
                }


                double score =
                        1.0;


                if (
                        text != null
                        &&
                        !text.isBlank()
                ) {

                    String haystack =
                            String.join(
                                    " ",
                                    document.title(),
                                    document.description(),
                                    document.buyerName(),
                                    String.join(
                                            " ",
                                            document.cpvCodes()
                                    ),
                                    String.join(
                                            " ",
                                            document.cpvLabels()
                                    )
                            )
                                    .toLowerCase(
                                            Locale.ROOT
                                    );


                    boolean allTokensMatch =
                            List.of(
                                    text.split(
                                            "\\s+"
                                    )
                            )
                                    .stream()
                                    .allMatch(
                                            haystack::contains
                                    );


                    if (
                            !allTokensMatch
                    ) {
                        continue;
                    }


                    if (
                            document
                                    .title()
                                    .toLowerCase(
                                            Locale.ROOT
                                    )
                                    .contains(
                                            text
                                    )
                    ) {
                        score =
                                5.0;

                    } else {
                        score =
                                2.0;
                    }

                }


                matches.add(
                        new SearchMatch(
                                document.id(),
                                score
                        )
                );

            }


            matches.sort(
                    Comparator.comparingDouble(
                            SearchMatch::score
                    )
                            .reversed()
            );


            int size =
                    query.size()
                            ==
                            null

                            ? 25

                            : Math.max(
                                    1,
                                    Math.min(
                                            100,
                                            query.size()
                                    )
                            );


            List<SearchMatch> bounded =
                    matches
                            .stream()
                            .limit(
                                    size
                            )
                            .toList();


            return new SearchPage(
                    matches.size(),
                    bounded
            );

        }

    }

}
