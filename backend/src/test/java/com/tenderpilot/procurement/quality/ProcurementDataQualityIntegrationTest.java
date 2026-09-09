package com.tenderpilot.procurement.quality;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.procurement.api.OpportunitySummaryResponse;

import com.tenderpilot.procurement.application.CanonicalTenderImportService;
import com.tenderpilot.procurement.application.CanonicalTenderUpsertRequest;
import com.tenderpilot.procurement.application.OpportunityQueryService;

import com.tenderpilot.procurement.award.application.CanonicalAwardImportService;
import com.tenderpilot.procurement.award.application.CanonicalAwardUpsertRequest;

import com.tenderpilot.procurement.domain.BuyerCategory;
import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementProcedure;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import com.tenderpilot.procurement.quality.domain.DataQualityGrade;
import com.tenderpilot.procurement.quality.domain.DataQualityIssueCode;
import com.tenderpilot.procurement.quality.domain.DataQualitySeverity;
import com.tenderpilot.procurement.quality.domain.ProcurementDataQualityAssessmentEntity;

import com.tenderpilot.procurement.quality.infrastructure.ProcurementDataQualityAssessmentRepository;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_quality;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
        }
)
@ActiveProfiles("test")
@Transactional
class ProcurementDataQualityIntegrationTest {

    @Autowired
    private CanonicalTenderImportService
            tenderImportService;


    @Autowired
    private CanonicalAwardImportService
            awardImportService;


    @Autowired
    private ProcurementDataQualityAssessmentRepository
            qualityRepository;


    @Autowired
    private OpportunityQueryService
            opportunityQueryService;


    @Test
    void completePublishedTenderIsAutomaticallyExcellent() {

        UUID tenderId =
                tenderImportService
                        .upsert(
                                completeRequest(
                                        "QUALITY-COMPLETE",
                                        ProcurementStatus.PUBLISHED,
                                        "Ville de Paris"
                                )
                        );


        ProcurementDataQualityAssessmentEntity quality =
                qualityRepository
                        .findByTender_Id(
                                tenderId
                        )
                        .orElseThrow();


        assertThat(
                quality.getScore()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                quality.getGrade()
        )
                .isEqualTo(
                        DataQualityGrade.EXCELLENT
                );


        assertThat(
                quality.getIssues()
        )
                .isEmpty();
    }


    @Test
    void sparsePublishedTenderProducesStructuredQualityIssues() {

        UUID tenderId =
                tenderImportService
                        .upsert(
                                sparseRequest(
                                        "QUALITY-SPARSE"
                                )
                        );


        ProcurementDataQualityAssessmentEntity quality =
                qualityRepository
                        .findByTender_Id(
                                tenderId
                        )
                        .orElseThrow();


        assertThat(
                quality.getScore()
        )
                .isLessThan(
                        60
                );


        assertThat(
                quality.getGrade()
        )
                .isIn(
                        DataQualityGrade.POOR,
                        DataQualityGrade.CRITICAL
                );


        assertThat(
                quality.getIssues()
        )
                .extracting(
                        issue ->
                                issue.getCode()
                )
                .contains(
                        DataQualityIssueCode.GENERIC_BUYER_IDENTITY,
                        DataQualityIssueCode.MISSING_CPV,
                        DataQualityIssueCode.MISSING_LOCATION,
                        DataQualityIssueCode.MISSING_SUBMISSION_DEADLINE,
                        DataQualityIssueCode.MISSING_ESTIMATED_VALUE,
                        DataQualityIssueCode.MISSING_SOURCE_URL
                );
    }


    @Test
    void awardedTenderWithoutAwardProducesCriticalConsistencyIssue() {

        UUID tenderId =
                tenderImportService
                        .upsert(
                                completeRequest(
                                        "QUALITY-AWARD-MISSING",
                                        ProcurementStatus.AWARDED,
                                        "Ville de Lyon"
                                )
                        );


        ProcurementDataQualityAssessmentEntity quality =
                qualityRepository
                        .findByTender_Id(
                                tenderId
                        )
                        .orElseThrow();


        assertThat(
                quality.getIssues()
        )
                .anySatisfy(
                        issue -> {

                            assertThat(
                                    issue.getCode()
                            )
                                    .isEqualTo(
                                            DataQualityIssueCode
                                                    .AWARD_RECORD_MISSING
                                    );


                            assertThat(
                                    issue.getSeverity()
                            )
                                    .isEqualTo(
                                            DataQualitySeverity.CRITICAL
                                    );

                        }
                );
    }


    @Test
    void awardImportAutomaticallyRecalculatesQuality() {

        UUID tenderId =
                tenderImportService
                        .upsert(
                                completeRequest(
                                        "QUALITY-AWARD-COMPLETE",
                                        ProcurementStatus.AWARDED,
                                        "Ville de Bordeaux"
                                )
                        );


        awardImportService
                .upsert(
                        tenderId,

                        new CanonicalAwardUpsertRequest(
                                LocalDate.of(
                                        2026,
                                        9,
                                        8
                                ),
                                new BigDecimal(
                                        "1600000"
                                ),
                                "EUR",
                                24,
                                "Révisable",
                                5,
                                null,
                                "Insertion professionnelle",
                                "Performance énergétique",
                                null,
                                null,
                                "Non",
                                List.of(
                                        new CanonicalAwardUpsertRequest
                                                .SupplierInput(
                                                        "12345678900011",
                                                        "SIRET",
                                                        "Quality Supplier SAS"
                                                )
                                )
                        )
                );


        ProcurementDataQualityAssessmentEntity quality =
                qualityRepository
                        .findByTender_Id(
                                tenderId
                        )
                        .orElseThrow();


        assertThat(
                quality.getScore()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                quality.getGrade()
        )
                .isEqualTo(
                        DataQualityGrade.EXCELLENT
                );


        assertThat(
                quality.getIssues()
        )
                .extracting(
                        issue ->
                                issue.getCode()
                )
                .doesNotContain(
                        DataQualityIssueCode.AWARD_RECORD_MISSING,
                        DataQualityIssueCode.AWARDED_AMOUNT_MISSING,
                        DataQualityIssueCode.AWARDED_SUPPLIER_MISSING
                );
    }


    @Test
    void opportunityQuerySupportsMinimumQualityFilter() {

        UUID completeId =
                tenderImportService
                        .upsert(
                                completeRequest(
                                        "QUALITY-FILTER-GOOD",
                                        ProcurementStatus.PUBLISHED,
                                        "Ville de Toulouse"
                                )
                        );


        tenderImportService
                .upsert(
                        sparseRequest(
                                "QUALITY-FILTER-SPARSE"
                        )
                );


        List<OpportunitySummaryResponse> result =
                opportunityQueryService
                        .list(
                                null,
                                null,
                                null,
                                90,
                                null
                        );


        assertThat(
                result
        )
                .extracting(
                        OpportunitySummaryResponse::id
                )
                .contains(
                        completeId
                );


        assertThat(
                result
        )
                .allSatisfy(
                        opportunity ->

                                assertThat(
                                        opportunity.dataQualityScore()
                                )
                                        .isGreaterThanOrEqualTo(
                                                90
                                        )
                );
    }


    private CanonicalTenderUpsertRequest completeRequest(
            String externalId,
            ProcurementStatus status,
            String buyerName
    ) {

        boolean open =
                status == ProcurementStatus.PUBLISHED
                ||
                status == ProcurementStatus.UPDATED;


        return new CanonicalTenderUpsertRequest(
                ProcurementSource.MANUAL_DEMO,

                externalId,

                "1",

                "QUALITY_FIXTURE",

                "https://example.test/notices/"
                        +
                        externalId,

                "quality-checksum-"
                        +
                        externalId,

                Instant.parse(
                        "2026-09-09T08:00:00Z"
                ),

                new CanonicalTenderUpsertRequest
                        .BuyerInput(
                                "QUALITY-BUYER:"
                                        +
                                        externalId,
                                buyerName,
                                BuyerCategory.LOCAL_AUTHORITY,
                                "FR",
                                "Paris",
                                null,
                                null
                        ),

                "Quality fixture "
                        +
                        externalId,

                "Complete procurement quality fixture.",

                status,

                ProcurementProcedure.OPEN,

                ContractNature.WORKS,

                LocalDate.of(
                        2026,
                        9,
                        9
                ),

                open
                        ? Instant.parse(
                                "2026-10-20T16:00:00Z"
                        )
                        : null,

                open
                        ? new BigDecimal(
                                "1000000"
                        )
                        : null,

                open
                        ? new BigDecimal(
                                "2000000"
                        )
                        : null,

                open
                        ? "EUR"
                        : null,

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


    private CanonicalTenderUpsertRequest sparseRequest(
            String externalId
    ) {

        return new CanonicalTenderUpsertRequest(
                ProcurementSource.MANUAL_DEMO,

                externalId,

                "1",

                "QUALITY_SPARSE_FIXTURE",

                null,

                "quality-sparse-"
                        +
                        externalId,

                Instant.parse(
                        "2026-09-09T08:00:00Z"
                ),

                new CanonicalTenderUpsertRequest
                        .BuyerInput(
                                "QUALITY-SPARSE-BUYER:"
                                        +
                                        externalId,
                                "TED contracting authority",
                                BuyerCategory.OTHER,
                                "FR",
                                null,
                                null,
                                null
                        ),

                "Sparse quality fixture "
                        +
                        externalId,

                "Sparse procurement fixture.",

                ProcurementStatus.PUBLISHED,

                ProcurementProcedure.OTHER,

                ContractNature.OTHER,

                LocalDate.of(
                        2026,
                        9,
                        9
                ),

                null,

                null,

                null,

                null,

                "FR",

                null,

                null,

                false,

                false,

                false,

                List.of(),

                List.of(),

                List.of()
        );
    }

}
