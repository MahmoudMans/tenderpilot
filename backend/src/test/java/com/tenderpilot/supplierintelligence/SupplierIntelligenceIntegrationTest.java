package com.tenderpilot.supplierintelligence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.supplierintelligence.application.SupplierIntelligenceService;
import com.tenderpilot.supplierintelligence.application.SupplierObservationGateway;

import com.tenderpilot.supplierintelligence.domain.BuyerConcentration;
import com.tenderpilot.supplierintelligence.domain.CpvSpecialization;
import com.tenderpilot.supplierintelligence.domain.SupplierAwardObservation;
import com.tenderpilot.supplierintelligence.domain.SupplierAwardParty;
import com.tenderpilot.supplierintelligence.domain.SupplierTenderCpv;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_supplier_intelligence;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=false",
                "tenderpilot.search.auto-index=false",
                "tenderpilot.local-ai.enabled=false",
                "tenderpilot.rag.enabled=false"
        }
)
@ActiveProfiles(
        "test"
)
@Import(
        SupplierIntelligenceIntegrationTest
                .FakeConfiguration.class
)
class SupplierIntelligenceIntegrationTest {

    private static final UUID AWARD_1 =
            UUID.fromString(
                    "18000000-0000-0000-0000-000000000001"
            );


    private static final UUID AWARD_2 =
            UUID.fromString(
                    "18000000-0000-0000-0000-000000000002"
            );


    private static final UUID AWARD_3 =
            UUID.fromString(
                    "18000000-0000-0000-0000-000000000003"
            );


    private static final UUID TENDER_1 =
            UUID.fromString(
                    "18100000-0000-0000-0000-000000000001"
            );


    private static final UUID TENDER_2 =
            UUID.fromString(
                    "18100000-0000-0000-0000-000000000002"
            );


    private static final UUID TENDER_3 =
            UUID.fromString(
                    "18100000-0000-0000-0000-000000000003"
            );


    private static final UUID BUYER_1 =
            UUID.fromString(
                    "18200000-0000-0000-0000-000000000001"
            );


    private static final UUID BUYER_2 =
            UUID.fromString(
                    "18200000-0000-0000-0000-000000000002"
            );


    @Autowired
    private SupplierIntelligenceService
            service;


    @Test
    void supplierSearchBuildsPublicAwardProfile() {

        var suppliers =
                service.search(
                        "Alpha",
                        "FR",
                        20
                );


        assertThat(
                suppliers
        )
                .hasSize(
                        1
                );


        var supplier =
                suppliers.getFirst();


        assertThat(
                supplier.name()
        )
                .isEqualTo(
                        "Supplier Alpha"
                );


        assertThat(
                supplier.awardCount()
        )
                .isEqualTo(
                        3
                );


        assertThat(
                supplier.uniqueBuyerCount()
        )
                .isEqualTo(
                        2
                );


        assertThat(
                supplier.repeatBuyerSignal()
        )
                .isTrue();


        assertThat(
                supplier.topBuyerSharePercentage()
        )
                .isEqualTo(
                        67
                );


        assertThat(
                supplier.buyerHhi()
        )
                .isEqualTo(
                        5556
                );


        assertThat(
                supplier.buyerConcentration()
        )
                .isEqualTo(
                        BuyerConcentration.CONCENTRATED
                );


        assertThat(
                supplier.topCpvSharePercentage()
        )
                .isEqualTo(
                        67
                );


        assertThat(
                supplier.cpvSpecialization()
        )
                .isEqualTo(
                        CpvSpecialization.FOCUSED
                );
    }


    @Test
    void detailContainsBuyerCpvCoAwardAndRecentAwardLandscape() {

        String supplierKey =
                service
                        .search(
                                "Alpha",
                                "FR",
                                20
                        )
                        .getFirst()
                        .supplierKey();


        var detail =
                service.detail(
                        supplierKey
                );


        assertThat(
                detail.topBuyers()
        )
                .hasSize(
                        2
                );


        assertThat(
                detail
                        .topBuyers()
                        .getFirst()
                        .awardCount()
        )
                .isEqualTo(
                        2
                );


        assertThat(
                detail.topCpvs()
        )
                .isNotEmpty();


        assertThat(
                detail.coAwardSuppliers()
        )
                .hasSize(
                        1
                );


        assertThat(
                detail
                        .coAwardSuppliers()
                        .getFirst()
                        .name()
        )
                .isEqualTo(
                        "Supplier Beta"
                );


        assertThat(
                detail.recentAwards()
        )
                .hasSize(
                        3
                );


        assertThat(
                detail
                        .recentAwards()
                        .stream()
                        .anyMatch(
                                recent ->
                                        recent.jointAward()
                        )
        )
                .isTrue();
    }


    @Test
    void countryFilterUsesObservedBuyerCountry() {

        assertThat(
                service.search(
                        "Alpha",
                        "FR",
                        20
                )
        )
                .hasSize(
                        1
                );


        assertThat(
                service.search(
                        "Alpha",
                        "BE",
                        20
                )
        )
                .isEmpty();
    }


    @Test
    void unknownSupplierKeyReturnsResourceNotFound() {

        String unknown =
                java.util.Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                "Unknown Supplier"
                                        .getBytes(
                                                java.nio.charset.StandardCharsets.UTF_8
                                        )
                        );


        assertThatThrownBy(
                () ->
                        service.detail(
                                unknown
                        )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                );
    }


    @TestConfiguration
    static class FakeConfiguration {

        @Bean
        @Primary
        SupplierObservationGateway supplierObservationGateway() {

            return new FakeSupplierObservationGateway();
        }

    }


    static class FakeSupplierObservationGateway
            implements SupplierObservationGateway {

        private final List<SupplierAwardObservation> observations =
                List.of(
                        observation(
                                "Supplier Alpha",
                                AWARD_1,
                                TENDER_1,
                                BUYER_1,
                                "Ville A",
                                "Paris",
                                LocalDate.of(
                                        2026,
                                        8,
                                        1
                                ),
                                "100000"
                        ),

                        observation(
                                "Supplier Alpha",
                                AWARD_2,
                                TENDER_2,
                                BUYER_1,
                                "Ville A",
                                "Paris",
                                LocalDate.of(
                                        2026,
                                        7,
                                        1
                                ),
                                "200000"
                        ),

                        observation(
                                "Supplier Alpha",
                                AWARD_3,
                                TENDER_3,
                                BUYER_2,
                                "Métropole B",
                                "Lyon",
                                LocalDate.of(
                                        2026,
                                        6,
                                        1
                                ),
                                "300000"
                        )
                );


        @Override
        public List<String> searchSupplierNames(
                String query,
                String buyerCountry,
                int limit
        ) {

            if (
                    buyerCountry != null
                    &&
                    !buyerCountry.equalsIgnoreCase(
                            "FR"
                    )
            ) {

                return List.of();
            }


            if (
                    query != null
                    &&
                    !(
                            "Supplier Alpha"
                                    .toLowerCase()
                                    .contains(
                                            query
                                                    .toLowerCase()
                                    )
                    )
            ) {

                return List.of();
            }


            return List.of(
                    "Supplier Alpha"
            );
        }


        @Override
        public List<SupplierAwardObservation> observationsBySupplier(
                String supplierName
        ) {

            if (
                    supplierName.equalsIgnoreCase(
                            "Supplier Alpha"
                    )
            ) {

                return observations;
            }


            return List.of();
        }


        @Override
        public List<SupplierAwardParty> suppliersForAwards(
                Collection<UUID> awardIds
        ) {

            List<SupplierAwardParty> result =
                    new ArrayList<>();


            for (
                    UUID awardId :
                    awardIds
            ) {

                result.add(
                        new SupplierAwardParty(
                                awardId,
                                "Supplier Alpha"
                        )
                );


                if (
                        awardId.equals(
                                AWARD_1
                        )
                ) {

                    result.add(
                            new SupplierAwardParty(
                                    awardId,
                                    "Supplier Beta"
                            )
                    );
                }
            }


            return List.copyOf(
                    result
            );
        }


        @Override
        public List<SupplierTenderCpv> cpvsForTenders(
                Collection<UUID> tenderIds
        ) {

            List<SupplierTenderCpv> result =
                    new ArrayList<>();


            for (
                    UUID tenderId :
                    tenderIds
            ) {

                if (
                        tenderId.equals(
                                TENDER_1
                        )
                        ||
                        tenderId.equals(
                                TENDER_2
                        )
                ) {

                    result.add(
                            new SupplierTenderCpv(
                                    tenderId,
                                    "45453000",
                                    "Overhaul and refurbishment work"
                            )
                    );


                } else if (
                        tenderId.equals(
                                TENDER_3
                        )
                ) {

                    result.add(
                            new SupplierTenderCpv(
                                    tenderId,
                                    "45210000",
                                    "Building construction work"
                            )
                    );
                }
            }


            return List.copyOf(
                    result
            );
        }


        private static SupplierAwardObservation observation(
                String supplierName,
                UUID awardId,
                UUID tenderId,
                UUID buyerId,
                String buyerName,
                String buyerCity,
                LocalDate publicationDate,
                String amount
        ) {

            return new SupplierAwardObservation(
                    supplierName,
                    awardId,
                    tenderId,
                    buyerId,
                    buyerName,
                    "FR",
                    buyerCity,
                    "Public contract "
                            +
                            tenderId,
                    "AWARDED",
                    publicationDate,
                    new BigDecimal(
                            amount
                    ),
                    "EUR"
            );
        }

    }

}
