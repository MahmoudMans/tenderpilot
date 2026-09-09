package com.tenderpilot.datasource.ted;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.procurement.application.CanonicalTenderUpsertRequest;

import com.tenderpilot.procurement.domain.BuyerCategory;
import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementProcedure;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import java.math.BigDecimal;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TedAdapterTest {

    private final TedSearchResponseParser
            parser =
            new TedSearchResponseParser();


    private final TedNoticeMapper
            mapper =
            new TedNoticeMapper(
                    new TedCountryCodeNormalizer()
            );


    @Test
    void buildsOfficialTedRecentPublicationDateQuery() {

        Clock clock =

                Clock.fixed(

                        Instant.parse(
                                "2026-09-09T12:00:00Z"
                        ),

                        ZoneOffset.UTC

                );


        TedQueryFactory factory =

                new TedQueryFactory(
                        clock
                );


        assertThat(
                factory.recentPublications(
                        7
                )
        )
                .isEqualTo(
                        "PD = (20260903 <> 20260909)"
                );

    }


    @Test
    void parsesPrimaryTedSearchEnvelope() {

        Map<String, Object> notice =

                Map.ofEntries(

                        Map.entry(
                                "publication-number",
                                "123456-2026"
                        ),

                        Map.entry(
                                "notice-version",
                                "01"
                        ),

                        Map.entry(
                                "notice-type",
                                "cn-standard"
                        ),

                        Map.entry(
                                "title-proc",
                                Map.of(
                                        "eng",
                                        "Energy renovation of public buildings"
                                )
                        ),

                        Map.entry(
                                "description-proc",
                                Map.of(
                                        "eng",
                                        "Deep renovation works."
                                )
                        ),

                        Map.entry(
                                "publication-date",
                                "20260909"
                        ),

                        Map.entry(
                                "buyer-name",
                                Map.of(
                                        "eng",
                                        "Demo Municipality"
                                )
                        ),

                        Map.entry(
                                "buyer-identifier",
                                "FR-DEMO-001"
                        ),

                        Map.entry(
                                "buyer-country",
                                "FRA"
                        ),

                        Map.entry(
                                "buyer-city",
                                "Paris"
                        ),

                        Map.entry(
                                "buyer-legal-type",
                                "rauth"
                        ),

                        Map.entry(
                                "procedure-type",
                                "open"
                        ),

                        Map.entry(
                                "contract-nature-main-proc",
                                "works"
                        ),

                        Map.entry(
                                "estimated-value-proc",
                                "2500000"
                        ),

                        Map.entry(
                                "estimated-value-cur-proc",
                                "EUR"
                        ),

                        Map.entry(
                                "deadline-receipt-tender-date-lot",
                                List.of(
                                        "20261020"
                                )
                        ),

                        Map.entry(
                                "deadline-receipt-tender-time-lot",
                                List.of(
                                        "16:00:00"
                                )
                        ),

                        Map.entry(
                                "classification-cpv",
                                List.of(
                                        "45453000",
                                        "45321000"
                                )
                        ),

                        Map.entry(
                                "place-of-performance-country-proc",
                                List.of(
                                        "FRA"
                                )
                        ),

                        Map.entry(
                                "place-of-performance-subdiv-proc",
                                List.of(
                                        "FR101"
                                )
                        ),

                        Map.entry(
                                "place-of-performance-city-proc",
                                List.of(
                                        "Paris"
                                )
                        ),

                        Map.entry(
                                "links",
                                Map.of(
                                        "html",
                                        "https://ted.europa.eu/en/notice/-/detail/123456-2026"
                                )
                        )

                );


        TedSearchPage page =

                parser.parse(

                        Map.of(

                                "total",
                                1,

                                "notices",
                                List.of(
                                        notice
                                )

                        )

                );


        assertThat(
                page.total()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                page.notices()
        )
                .hasSize(
                        1
                );


        TedSearchNotice parsed =
                page.notices()
                        .getFirst();


        assertThat(
                parsed.externalId()
        )
                .isEqualTo(
                        "123456-2026"
                );


        assertThat(
                parsed.title()
        )
                .isEqualTo(
                        "Energy renovation of public buildings"
                );


        assertThat(
                parsed.publicationDate()
        )
                .isEqualTo(
                        LocalDate.of(
                                2026,
                                9,
                                9
                        )
                );


        assertThat(
                parsed.cpvCodes()
        )
                .containsExactly(
                        "45453000",
                        "45321000"
                );


        assertThat(
                parsed.sourceUrl()
        )
                .contains(
                        "ted.europa.eu"
                );

    }


    @Test
    void parsesAlternativeResultsEnvelopeAndMultilingualValues() {

        TedSearchPage page =

                parser.parse(

                        Map.of(

                                "totalResults",
                                "1",

                                "results",
                                List.of(

                                        Map.of(

                                                "publicationNumber",
                                                "654321-2026",

                                                "noticeTitle",
                                                Map.of(

                                                        "fra",
                                                        "Travaux publics",

                                                        "eng",
                                                        "Public works"

                                                ),

                                                "publicationDate",
                                                "2026-09-08"

                                        )

                                )

                        )

                );


        assertThat(
                page.notices()
                        .getFirst()
                        .title()
        )
                .isEqualTo(
                        "Public works"
                );


        assertThat(
                page.notices()
                        .getFirst()
                        .publicationDate()
        )
                .isEqualTo(
                        LocalDate.of(
                                2026,
                                9,
                                8
                        )
                );

    }


    @Test
    void mapsTedNoticeIntoCanonicalProcurementContract() {

        CanonicalTenderUpsertRequest request =

                mapper.map(
                        sampleNotice(
                                LocalDate.of(
                                        2026,
                                        9,
                                        9
                                )
                        )
                );


        assertThat(
                request.source()
        )
                .isEqualTo(
                        ProcurementSource.TED
                );


        assertThat(
                request.status()
        )
                .isEqualTo(
                        ProcurementStatus.PUBLISHED
                );


        assertThat(
                request.procedureType()
        )
                .isEqualTo(
                        ProcurementProcedure.OPEN
                );


        assertThat(
                request.contractNature()
        )
                .isEqualTo(
                        ContractNature.WORKS
                );


        assertThat(
                request.buyer()
                        .category()
        )
                .isEqualTo(
                        BuyerCategory.LOCAL_AUTHORITY
                );


        assertThat(
                request.countryCode()
        )
                .isEqualTo(
                        "FR"
                );


        assertThat(
                request.cpvs()
        )
                .extracting(
                        CanonicalTenderUpsertRequest
                                .CpvInput
                                ::code
                )
                .containsExactly(
                        "45453000",
                        "45321000"
                );


        assertThat(
                request.submissionDeadline()
        )
                .isEqualTo(
                        Instant.parse(
                                "2026-10-20T16:00:00Z"
                        )
                );

    }


    @Test
    void rejectsTedNoticeWithoutPublicationDate() {

        assertThatThrownBy(
                () ->
                        mapper.map(
                                sampleNotice(
                                        null
                                )
                        )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                )
                .hasMessageContaining(
                        "publication date"
                );

    }


    private TedSearchNotice sampleNotice(
            LocalDate publicationDate
    ) {

        return new TedSearchNotice(

                "777777-2026",

                "01",

                "cn-standard",

                "Municipal building renovation",

                "Energy renovation of municipal facilities.",

                publicationDate,

                new TedBuyerData(

                        "Demo Municipality",

                        "FR-DEMO-001",

                        "FRA",

                        "Paris",

                        "https://example.test",

                        "rauth"

                ),

                "open",

                "works",

                List.of(
                        LocalDate.of(
                                2026,
                                10,
                                20
                        )
                ),

                LocalTime.of(
                        16,
                        0
                ),

                new BigDecimal(
                        "2500000"
                ),

                "EUR",

                List.of(
                        "45453000",
                        "45321000"
                ),

                List.of(

                        new TedLocationData(

                                "FRA",

                                "FR101",

                                "Paris"

                        )

                ),

                false,

                true,

                "https://ted.europa.eu/en/notice/-/detail/777777-2026"

        );

    }

}
