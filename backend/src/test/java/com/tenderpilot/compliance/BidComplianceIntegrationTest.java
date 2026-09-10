package com.tenderpilot.compliance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.biddecision.domain.BidAssessmentEvaluation;
import com.tenderpilot.biddecision.domain.BidDecisionEntity;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;
import com.tenderpilot.biddecision.domain.BidDecisionScorecard;

import com.tenderpilot.biddecision.infrastructure.BidDecisionRepository;

import com.tenderpilot.bidworkspace.api.CreateBidRequirementRequest;

import com.tenderpilot.bidworkspace.application.BidWorkspaceService;

import com.tenderpilot.bidworkspace.domain.BidRequirementCategory;
import com.tenderpilot.bidworkspace.domain.BidRequirementStatus;

import com.tenderpilot.bidworkspace.infrastructure.BidRequirementRepository;

import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.compliance.api.UpdateComplianceReviewRequest;

import com.tenderpilot.compliance.application.BidComplianceService;

import com.tenderpilot.compliance.domain.ComplianceRiskLevel;
import com.tenderpilot.compliance.domain.ComplianceVerdict;
import com.tenderpilot.compliance.domain.EvidenceStrength;

import com.tenderpilot.compliance.infrastructure.BidComplianceReviewRepository;

import com.tenderpilot.matching.domain.TenderMatchEntity;
import com.tenderpilot.matching.domain.TenderMatchEvaluation;
import com.tenderpilot.matching.domain.TenderMatchGrade;

import com.tenderpilot.matching.infrastructure.TenderMatchRepository;

import com.tenderpilot.procurement.application.CanonicalTenderImportService;
import com.tenderpilot.procurement.application.CanonicalTenderUpsertRequest;

import com.tenderpilot.procurement.domain.BuyerCategory;
import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementProcedure;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import com.tenderpilot.procurement.infrastructure.TenderRepository;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_compliance;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=false",
                "tenderpilot.search.auto-index=false",
                "tenderpilot.local-ai.enabled=false",
                "tenderpilot.rag.enabled=false"
        }
)
@ActiveProfiles(
        "test"
)
@Transactional
class BidComplianceIntegrationTest {

    private static final UUID ORG_ONE =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000001501"
            );


    private static final UUID ORG_TWO =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000001502"
            );


    @Autowired
    private CanonicalTenderImportService
            tenderImportService;


    @Autowired
    private TenderRepository
            tenderRepository;


    @Autowired
    private TenderMatchRepository
            matchRepository;


    @Autowired
    private BidDecisionRepository
            decisionRepository;


    @Autowired
    private BidRequirementRepository
            requirementRepository;


    @Autowired
    private BidComplianceReviewRepository
            reviewRepository;


    @Autowired
    private BidWorkspaceService
            workspaceService;


    @Autowired
    private BidComplianceService
            complianceService;


    @BeforeEach
    void clean() {

        reviewRepository.deleteAll();

        requirementRepository.deleteAll();

        decisionRepository.deleteAll();

        matchRepository.deleteAll();

        reviewRepository.flush();

        requirementRepository.flush();

        decisionRepository.flush();

        matchRepository.flush();
    }


    @Test
    void newRequirementAppearsUnassessedInComplianceMatrix() {

        UUID tenderId =
                seedTenderAndMatch(
                        "COMPLIANCE-EMPTY",
                        ORG_ONE
                );


        createRequirement(
                tenderId,
                true,
                "Provide three comparable references"
        );


        var matrix =
                complianceService
                        .matrix(
                                ORG_ONE,
                                tenderId
                        );


        assertThat(
                matrix.rows()
        )
                .hasSize(
                        1
                );


        assertThat(
                matrix
                        .rows()
                        .getFirst()
                        .verdict()
        )
                .isEqualTo(
                        ComplianceVerdict.UNASSESSED
                );


        assertThat(
                matrix
                        .statistics()
                        .mandatoryIncompleteCount()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                matrix
                        .statistics()
                        .readyForSubmission()
        )
                .isFalse();
    }


    @Test
    void compliantReviewSynchronizesRequirementStatus() {

        UUID tenderId =
                seedTenderAndMatch(
                        "COMPLIANCE-SYNC",
                        ORG_ONE
                );


        UUID requirementId =
                createRequirement(
                        tenderId,
                        true,
                        "Provide ISO 9001 certificate"
                );


        var matrix =
                complianceService
                        .review(
                                ORG_ONE,

                                tenderId,

                                requirementId,

                                new UpdateComplianceReviewRequest(
                                        ComplianceVerdict.COMPLIANT,
                                        ComplianceRiskLevel.LOW,
                                        EvidenceStrength.STRONG,
                                        "Certificate is current and covers the required activity.",
                                        "Preserve certificate in final evidence pack."
                                ),

                                "analyst@example.test"
                        );


        assertThat(
                matrix
                        .rows()
                        .getFirst()
                        .verdict()
        )
                .isEqualTo(
                        ComplianceVerdict.COMPLIANT
                );


        assertThat(
                matrix
                        .rows()
                        .getFirst()
                        .requirementStatus()
        )
                .isEqualTo(
                        BidRequirementStatus.SATISFIED
                );


        assertThat(
                requirementRepository
                        .findById(
                                requirementId
                        )
                        .orElseThrow()
                        .getStatus()
        )
                .isEqualTo(
                        BidRequirementStatus.SATISFIED
                );


        assertThat(
                reviewRepository.count()
        )
                .isEqualTo(
                        1
                );
    }


    @Test
    void mandatoryNonComplianceCreatesReadinessBlocker() {

        UUID tenderId =
                seedTenderAndMatch(
                        "COMPLIANCE-BLOCKER",
                        ORG_ONE
                );


        UUID requirementId =
                createRequirement(
                        tenderId,
                        true,
                        "Provide required security clearance"
                );


        var matrix =
                complianceService
                        .review(
                                ORG_ONE,

                                tenderId,

                                requirementId,

                                new UpdateComplianceReviewRequest(
                                        ComplianceVerdict.NON_COMPLIANT,
                                        ComplianceRiskLevel.CRITICAL,
                                        EvidenceStrength.NONE,
                                        "Required clearance is not currently held.",
                                        "Obtain clearance before submission or withdraw."
                                ),

                                "analyst@example.test"
                        );


        assertThat(
                matrix
                        .statistics()
                        .mandatoryBlockerCount()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                matrix
                        .statistics()
                        .criticalRiskCount()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                matrix
                        .statistics()
                        .readyForSubmission()
        )
                .isFalse();


        assertThat(
                matrix
                        .rows()
                        .getFirst()
                        .requirementStatus()
        )
                .isEqualTo(
                        BidRequirementStatus.GAP
                );
    }


    @Test
    void complianceMatrixIsTenantIsolated() {

        UUID tenderId =
                seedTenderAndMatch(
                        "COMPLIANCE-TENANT",
                        ORG_ONE
                );


        createRequirement(
                tenderId,
                true,
                "Tenant-private requirement"
        );


        assertThatThrownBy(
                () ->
                        complianceService
                                .matrix(
                                        ORG_TWO,
                                        tenderId
                                )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                );
    }


    @Test
    void noGoDecisionPreservesReadAccessButLocksReviews() {

        UUID tenderId =
                seedTenderAndMatch(
                        "COMPLIANCE-NOGO",
                        ORG_ONE
                );


        UUID requirementId =
                createRequirement(
                        tenderId,
                        true,
                        "Technical methodology"
                );


        TenderMatchEntity match =
                matchRepository
                        .findByOrganizationIdAndTender_Id(
                                ORG_ONE,
                                tenderId
                        )
                        .orElseThrow();


        BidDecisionEntity decision =
                BidDecisionEntity
                        .create(
                                ORG_ONE,
                                match.getTender()
                        );


        decision.applyAssessment(
                new BidDecisionScorecard(
                        5,
                        5,
                        5,
                        5,
                        5,
                        5
                ),

                new BidAssessmentEvaluation(
                        100,
                        BidDecisionOutcome.GO,
                        "Synthetic recommendation"
                ),

                90,

                TenderMatchGrade.EXCELLENT,

                "Strong fit",

                "No material risks",

                "Stable assumptions"
        );


        decision.decide(
                BidDecisionOutcome.NO_GO,
                "manager@example.test"
        );


        decisionRepository
                .saveAndFlush(
                        decision
                );


        var matrix =
                complianceService
                        .matrix(
                                ORG_ONE,
                                tenderId
                        );


        assertThat(
                matrix.writable()
        )
                .isFalse();


        assertThatThrownBy(
                () ->
                        complianceService
                                .review(
                                        ORG_ONE,

                                        tenderId,

                                        requirementId,

                                        new UpdateComplianceReviewRequest(
                                                ComplianceVerdict.COMPLIANT,
                                                ComplianceRiskLevel.LOW,
                                                EvidenceStrength.STRONG,
                                                "Synthetic",
                                                null
                                        ),

                                        "analyst@example.test"
                                )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                );
    }


    private UUID createRequirement(
            UUID tenderId,
            boolean mandatory,
            String title
    ) {

        var workspace =
                workspaceService
                        .createRequirement(
                                ORG_ONE,

                                tenderId,

                                new CreateBidRequirementRequest(
                                        title,

                                        "Requirement fixture for compliance intelligence.",

                                        BidRequirementCategory.TECHNICAL,

                                        mandatory,

                                        null,

                                        "CCTP §4",

                                        LocalDate.of(
                                                2026,
                                                10,
                                                20
                                        ),

                                        "Bid team",

                                        null
                                ),

                                "analyst@example.test"
                        );


        return workspace
                .requirements()
                .stream()
                .filter(
                        requirement ->
                                requirement.title()
                                        .equals(
                                                title
                                        )
                )
                .findFirst()
                .orElseThrow()
                .id();
    }


    private UUID seedTenderAndMatch(
            String externalId,
            UUID organizationId
    ) {

        UUID tenderId =
                tenderImportService
                        .upsert(
                                tender(
                                        externalId
                                )
                        );


        var tender =
                tenderRepository
                        .findById(
                                tenderId
                        )
                        .orElseThrow();


        TenderMatchEntity match =
                TenderMatchEntity
                        .create(
                                organizationId,
                                tender
                        );


        match.apply(
                new TenderMatchEvaluation(
                        90,
                        TenderMatchGrade.EXCELLENT,
                        false,
                        25,
                        15,
                        15,
                        10,
                        15,
                        5,
                        5,
                        List.of()
                )
        );


        matchRepository
                .saveAndFlush(
                        match
                );


        return tenderId;
    }


    private CanonicalTenderUpsertRequest tender(
            String externalId
    ) {

        return new CanonicalTenderUpsertRequest(
                ProcurementSource.MANUAL_DEMO,

                externalId,

                "1",

                "COMPLIANCE_TEST",

                "https://example.test/"
                        +
                        externalId,

                "compliance-checksum-"
                        +
                        externalId,

                Instant.parse(
                        "2026-09-10T08:00:00Z"
                ),

                new CanonicalTenderUpsertRequest
                        .BuyerInput(
                                "COMPLIANCE-BUYER:"
                                        +
                                        externalId,

                                "Ville de Paris",

                                BuyerCategory.LOCAL_AUTHORITY,

                                "FR",

                                "Paris",

                                null,

                                null
                        ),

                "Compliance tender "
                        +
                        externalId,

                "Public procurement compliance test.",

                ProcurementStatus.PUBLISHED,

                ProcurementProcedure.OPEN,

                ContractNature.WORKS,

                LocalDate.of(
                        2026,
                        9,
                        10
                ),

                Instant.parse(
                        "2026-10-30T16:00:00Z"
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

}
