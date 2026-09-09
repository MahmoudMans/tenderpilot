package com.tenderpilot.datasource.boamp;

import static org.assertj.core.api.Assertions.assertThat;

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
import java.time.ZoneOffset;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class BoampAdapterTest {

    private final BoampSearchResponseParser
            parser =
            new BoampSearchResponseParser();


    private final BoampNoticeMapper
            mapper =
            new BoampNoticeMapper(

                    new BoampDepartmentRegionNormalizer()

            );


    @Test
    void buildsBoundedOfficialOpendatasoftDateRange() {

        Clock clock =

                Clock.fixed(

                        Instant.parse(
                                "2026-09-09T12:00:00Z"
                        ),

                        ZoneOffset.UTC

                );


        BoampQueryFactory factory =

                new BoampQueryFactory(
                        clock
                );


        assertThat(
                factory.recentPublications(
                        7
                )
        )
                .isEqualTo(
                        "dateparution IN [date'2026-09-03'..date'2026-09-09']"
                );

    }


    @Test
    void parsesExploreApiV21ResultsEnvelope() {

        Map<String, Object> result =

                Map.ofEntries(

                        Map.entry(
                                "idweb",
                                "26-123456"
                        ),

                        Map.entry(
                                "id",
                                "26_123456"
                        ),

                        Map.entry(
                                "objet",
                                "Rénovation énergétique d'un groupe scolaire"
                        ),

                        Map.entry(
                                "dateparution",
                                "2026-09-09"
                        ),

                        Map.entry(
                                "datelimitereponse",
                                "2026-10-20T16:00:00+02:00"
                        ),

                        Map.entry(
                                "nomacheteur",
                                "Ville de Paris"
                        ),

                        Map.entry(
                                "procedure_libelle",
                                "Appel d'offres ouvert"
                        ),

                        Map.entry(
                                "type_marche_facette",
                                "Travaux"
                        ),

                        Map.entry(
                                "type_avis",
                                "Avis de marché"
                        ),

                        Map.entry(
                                "source_schema",
                                "eForms"
                        ),

                        Map.entry(
                                "code_departement_prestation",
                                "75"
                        ),

                        Map.entry(
                                "url_avis",
                                "https://www.boamp.fr/avis/detail/26-123456"
                        ),

                        Map.entry(
                                "DONNEES",
                                "{\"cpv\":\"45453000\",\"classification\":\"45321000\"}"
                        )

                );


        BoampSearchPage page =

                parser.parse(

                        Map.of(

                                "total_count",
                                1,

                                "results",
                                List.of(
                                        result
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


        BoampNotice notice =
                page.notices()
                        .getFirst();


        assertThat(
                notice.externalId()
        )
                .isEqualTo(
                        "26-123456"
                );


        assertThat(
                notice.publicationDate()
        )
                .isEqualTo(
                        LocalDate.of(
                                2026,
                                9,
                                9
                        )
                );


        assertThat(
                notice.cpvCodes()
        )
                .contains(
                        "45453000",
                        "45321000"
                );

    }


    @Test
    void supportsLegacyWrappedExploreEnvelope() {

        Map<String, Object> fields =

                Map.of(

                        "idweb",
                        "26-LEGACY",

                        "objet",
                        "Legacy API wrapper fixture",

                        "dateparution",
                        "2026-09-08",

                        "nomacheteur",
                        "Commune de Lyon"

                );


        Map<String, Object> wrapped =

                Map.of(

                        "record",

                        Map.of(

                                "fields",
                                fields

                        )

                );


        BoampSearchPage page =

                parser.parse(

                        Map.of(

                                "total_count",
                                1,

                                "records",
                                List.of(
                                        wrapped
                                )

                        )

                );


        assertThat(
                page.notices()
                        .getFirst()
                        .externalId()
        )
                .isEqualTo(
                        "26-LEGACY"
                );


        assertThat(
                page.notices()
                        .getFirst()
                        .buyerName()
        )
                .isEqualTo(
                        "Commune de Lyon"
                );

    }


    @Test
    void mapsFrenchBoampNoticeIntoCanonicalProcurementModel() {

        CanonicalTenderUpsertRequest request =

                mapper.map(
                        currentNotice()
                );


        assertThat(
                request.source()
        )
                .isEqualTo(
                        ProcurementSource.BOAMP
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
                request.primaryRegionCode()
        )
                .isEqualTo(
                        "IDF"
                );


        assertThat(
                request.primaryRegionName()
        )
                .isEqualTo(
                        "Île-de-France"
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
                        "45453000"
                );

    }


    @Test
    void mapsAwardNoticeToAwardedStatus() {

        BoampNotice notice =

                new BoampNotice(

                        "26-654321",

                        "26_654321",

                        "Attribution d'un marché de rénovation",

                        LocalDate.of(
                                2026,
                                9,
                                9
                        ),

                        null,

                        "Commune de Bordeaux",

                        "Procédure ouverte",

                        "Travaux",

                        "Avis d'attribution",

                        "eForms",

                        "https://www.boamp.fr/avis/detail/26-654321",

                        "33",

                        List.of(),

                        new BigDecimal(
                                "900000"
                        ),

                        "EUR"

                );


        CanonicalTenderUpsertRequest request =

                mapper.map(
                        notice
                );


        assertThat(
                request.status()
        )
                .isEqualTo(
                        ProcurementStatus.AWARDED
                );


        assertThat(
                request.primaryRegionCode()
        )
                .isEqualTo(
                        "NAQ"
                );

    }


    private BoampNotice currentNotice() {

        return new BoampNotice(

                "26-123456",

                "26_123456",

                "Rénovation énergétique d'un groupe scolaire",

                LocalDate.of(
                        2026,
                        9,
                        9
                ),

                Instant.parse(
                        "2026-10-20T14:00:00Z"
                ),

                "Ville de Paris",

                "Appel d'offres ouvert",

                "Travaux",

                "Avis de marché",

                "eForms",

                "https://www.boamp.fr/avis/detail/26-123456",

                "75",

                List.of(
                        "45453000"
                ),

                new BigDecimal(
                        "2500000"
                ),

                "EUR"

        );

    }

}
