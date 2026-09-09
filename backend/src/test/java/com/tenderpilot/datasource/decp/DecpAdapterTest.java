package com.tenderpilot.datasource.decp;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.datasource.boamp.BoampDepartmentRegionNormalizer;

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

class DecpAdapterTest {

    private final DecpExecutionLocationNormalizer
            locationNormalizer =
            new DecpExecutionLocationNormalizer(
                    new BoampDepartmentRegionNormalizer()
            );

    private final DecpAwardMapper mapper =
            new DecpAwardMapper(
                    new DecpSettings(
                            "https://data.economie.gouv.fr",
                            "/api/explore/v2.1/catalog/datasets/decp-2022-marches-valides/records",
                            "decp-2022-marches-valides",
                            7,
                            25,
                            100,
                            true
                    ),
                    locationNormalizer,
                    new DecpBuyerClassifier()
            );

    @Test
    void buildsRecentPublicationQuery() {

        DecpQueryFactory factory =
                new DecpQueryFactory(
                        Clock.fixed(
                                Instant.parse(
                                        "2026-09-09T12:00:00Z"
                                ),
                                ZoneOffset.UTC
                        )
                );

        assertThat(
                factory.recentPublications(7)
        )
                .isEqualTo(
                        "datepublicationdonnees IN [date'2026-09-03'..date'2026-09-09']"
                );
    }

    @Test
    void parsesExploreRecordAndSupplier() {

        DecpSearchResponseParser parser =
                new DecpSearchResponseParser();

        DecpSearchPage page =
                parser.parse(
                        Map.of(
                                "total_count",
                                1,

                                "results",
                                List.of(
                                        Map.ofEntries(
                                                Map.entry(
                                                        "id",
                                                        "DECP-2026-001"
                                                ),
                                                Map.entry(
                                                        "objet",
                                                        "Rénovation énergétique"
                                                ),
                                                Map.entry(
                                                        "codecpv",
                                                        "45453000-7"
                                                ),
                                                Map.entry(
                                                        "procedure",
                                                        "Appel d'offres ouvert"
                                                ),
                                                Map.entry(
                                                        "acheteur_id",
                                                        "21750001600019"
                                                ),
                                                Map.entry(
                                                        "acheteur_nom",
                                                        "Ville de Paris"
                                                ),
                                                Map.entry(
                                                        "dureemois",
                                                        24
                                                ),
                                                Map.entry(
                                                        "datenotification",
                                                        "2026-09-01"
                                                ),
                                                Map.entry(
                                                        "datepublicationdonnees",
                                                        "2026-09-08"
                                                ),
                                                Map.entry(
                                                        "montant",
                                                        2450000
                                                ),
                                                Map.entry(
                                                        "formeprix",
                                                        "Révisable"
                                                ),
                                                Map.entry(
                                                        "offresrecues",
                                                        6
                                                ),
                                                Map.entry(
                                                        "lieuexecution_code",
                                                        "75101"
                                                ),
                                                Map.entry(
                                                        "lieuexecution_typecode",
                                                        "Code commune"
                                                ),
                                                Map.entry(
                                                        "lieuexecution_nom",
                                                        "Paris"
                                                ),
                                                Map.entry(
                                                        "titulaire_id_1",
                                                        "55210055400013"
                                                ),
                                                Map.entry(
                                                        "titulaire_typeidentifiant_1",
                                                        "SIRET"
                                                ),
                                                Map.entry(
                                                        "titulaire_denominationsociale_1",
                                                        "Entreprise Démonstration"
                                                )
                                        )
                                )
                        )
                );

        DecpAwardRecord record =
                page.records().getFirst();

        assertThat(record.externalId())
                .isEqualTo(
                        "DECP-2026-001"
                );

        assertThat(record.cpvCode())
                .isEqualTo(
                        "45453000"
                );

        assertThat(record.awardedAmount())
                .isEqualByComparingTo(
                        "2450000"
                );

        assertThat(record.suppliers())
                .hasSize(1);

        assertThat(
                record.suppliers()
                        .getFirst()
                        .legalName()
        )
                .isEqualTo(
                        "Entreprise Démonstration"
                );
    }

    @Test
    void supportsNestedSupplierRepresentation() {

        DecpSearchResponseParser parser =
                new DecpSearchResponseParser();

        DecpSearchPage page =
                parser.parse(
                        Map.of(
                                "results",
                                List.of(
                                        Map.of(
                                                "id",
                                                "DECP-NESTED",

                                                "objet",
                                                "Nested supplier fixture",

                                                "datepublicationdonnees",
                                                "2026-09-09",

                                                "titulaires",
                                                List.of(
                                                        Map.of(
                                                                "id",
                                                                "99999999900011",
                                                                "typeIdentifiant",
                                                                "SIRET",
                                                                "denominationSociale",
                                                                "Nested Supplier SAS"
                                                        )
                                                )
                                        )
                                )
                        )
                );

        assertThat(
                page.records()
                        .getFirst()
                        .suppliers()
        )
                .hasSize(1);

        assertThat(
                page.records()
                        .getFirst()
                        .suppliers()
                        .getFirst()
                        .legalName()
        )
                .isEqualTo(
                        "Nested Supplier SAS"
                );
    }

    @Test
    void keepsAwardAmountSeparateFromEstimate() {

        DecpMappedContract mapped =
                mapper.map(fixture());

        assertThat(
                mapped.tender().source()
        )
                .isEqualTo(
                        ProcurementSource.DECP
                );

        assertThat(
                mapped.tender().status()
        )
                .isEqualTo(
                        ProcurementStatus.AWARDED
                );

        assertThat(
                mapped.tender()
                        .estimatedMinValue()
        )
                .isNull();

        assertThat(
                mapped.tender()
                        .estimatedMaxValue()
        )
                .isNull();

        assertThat(
                mapped.award()
                        .awardedAmount()
        )
                .isEqualByComparingTo(
                        "2450000"
                );

        assertThat(
                mapped.award().currency()
        )
                .isEqualTo("EUR");
    }

    @Test
    void derivesWorksAndOpenProcedure() {

        DecpMappedContract mapped =
                mapper.map(fixture());

        assertThat(
                mapped.tender()
                        .contractNature()
        )
                .isEqualTo(
                        ContractNature.WORKS
                );

        assertThat(
                mapped.tender()
                        .procedureType()
        )
                .isEqualTo(
                        ProcurementProcedure.OPEN
                );
    }

    @Test
    void classifiesVilleAndNormalizesParis() {

        DecpMappedContract mapped =
                mapper.map(fixture());

        assertThat(
                mapped.tender()
                        .buyer()
                        .category()
        )
                .isEqualTo(
                        BuyerCategory.LOCAL_AUTHORITY
                );

        assertThat(
                mapped.tender()
                        .primaryRegionCode()
        )
                .isEqualTo("IDF");

        assertThat(
                mapped.tender()
                        .primaryRegionName()
        )
                .isEqualTo(
                        "Île-de-France"
                );
    }

    private DecpAwardRecord fixture() {

        return new DecpAwardRecord(
                "DECP-2026-001",
                "Marché",
                "Rénovation énergétique d'un bâtiment public",
                "45453000-7",
                "Appel d'offres ouvert",
                "21750001600019",
                "Ville de Paris",
                24,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 8),
                new BigDecimal("2450000"),
                "Révisable",
                6,
                null,
                "75101",
                "Code commune",
                "Paris",
                "Insertion professionnelle",
                "Performance énergétique",
                "Exécution ordinaire",
                "Conjoint",
                "Non",
                "data.gouv.fr",
                List.of(
                        new DecpSupplierData(
                                "55210055400013",
                                "SIRET",
                                "Entreprise Démonstration"
                        )
                )
        );
    }
}
